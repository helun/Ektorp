package org.ektorp.spring;

import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

import org.ektorp.dataload.*;
import org.slf4j.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.scheduling.concurrent.*;
import org.springframework.stereotype.*;

/**
 * Multi-threaded data loader.
 * Will look up all components in the application context that implements the org.ektorp.dataload.DataLoader interface.
 * 
 * @author henrik lundgren
 *
 */
@Component("initialDataLoader")
public class InitialDataLoader implements InitializingBean {

	private final static Charset UTF_8 = Charset.forName("UTF-8");
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final List<DataLoader> loaders;
	private final ResourceLoader resources;
	
	@Autowired
	public InitialDataLoader(List<DataLoader> l, ResourceLoader rl) {
		loaders = l;
		resources = rl;
	}
	
	public void loadData() {
		ExecutorService es = Executors.newFixedThreadPool(loaders.size(), new CustomizableThreadFactory("initial-dataloader-"));
		for (final DataLoader l : loaders) {
			es.submit(new LoaderTask(l));
		}
		es.shutdown();
		try {
			afterLoad(es);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void afterLoad(ExecutorService es) throws InterruptedException {
		es.awaitTermination(5, TimeUnit.MINUTES);
		if (es.isTerminated()) {
			for (DataLoader l : loaders) {
				l.allDataLoaded();
			}	
		} else {
			log.error("The following data loaders did not complete in time: ");
			for(Runnable r : es.shutdownNow()) {
				log.error("The following data loaders did not complete in time: {}", r);	
			}
		}
	}
	
	private class LoaderTask implements Runnable {
		
		final DataLoader loader;
		
		LoaderTask(DataLoader l) {
			this.loader = l;
		}
		
		public void run() {
			for (String location: loader.getDataLocations()) {
				Resource data = resources.getResource(location);
				if (data.exists()) {
					try {
						log.info("loading data from {}", data.getDescription());
						loader.loadInitialData(new InputStreamReader(data.getInputStream(), UTF_8));
					} catch (Exception e) {
						log.error("Failed to load data from : " + location, e);
					}
				} else {
					log.error("Failed to load data from {} does not exists", data.getDescription());
				}	
			}
		}
		
		@Override
		public String toString() {
			return "LoaderTask: " + loader.getClass().getSimpleName();
		}
	}

	public void afterPropertiesSet() throws Exception {
		loadData();
	}
}
