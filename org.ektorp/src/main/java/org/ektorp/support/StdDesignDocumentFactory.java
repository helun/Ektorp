package org.ektorp.support;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.ektorp.util.*;

/**
 * 
 * @author henrik lundgren
 *
 */
public class StdDesignDocumentFactory implements DesignDocumentFactory {

	public SimpleViewGenerator viewGenerator = new SimpleViewGenerator();
	
	/* (non-Javadoc)
	 * @see org.ektorp.support.DesignDocumentFactory#generateFrom(java.lang.Object)
	 */
	public DesignDocument generateFrom(Object metaDataSource) {
		Class<?> metaDataClass = metaDataSource.getClass();
		DesignDocument dd = new DesignDocument();
		Map<String, DesignDocument.View> views = viewGenerator.generateViews(metaDataSource);
		dd.setViews(views);
		
		Map<String, String> lists = createListFunctions(metaDataClass);
		dd.setLists(lists);
		
		Map<String, String> shows = createShowFunctions(metaDataClass);
		dd.setShows(shows);
		
		Map<String, String> filters = createFilterFunctions(metaDataClass);
		dd.setFilters(filters);
		
		return dd;
	}
	
	private Map<String, String> createFilterFunctions(final Class<?> metaDataClass) {
		final Map<String, String> shows = new HashMap<String, String>();
		
		ReflectionUtils.eachAnnotation(metaDataClass, Filter.class, new Predicate<Filter>() {
			public boolean apply(Filter input) {
				shows.put(input.name(), resolveFilterFunction(input, metaDataClass));
				return true;
			}

		});
		
		ReflectionUtils.eachAnnotation(metaDataClass, Filters.class, new Predicate<Filters>() {
			public boolean apply(Filters input) {
				for (Filter sf : input.value()) {
					shows.put(sf.name(), resolveFilterFunction(sf, metaDataClass));
				}
				return true;
			}
		});
		
		return shows;
	}
	
	private Map<String, String> createShowFunctions(final Class<?> metaDataClass) {
		final Map<String, String> shows = new HashMap<String, String>();
		
		ReflectionUtils.eachAnnotation(metaDataClass, ShowFunction.class, new Predicate<ShowFunction>() {
			public boolean apply(ShowFunction input) {
				shows.put(input.name(), resolveShowFunction(input, metaDataClass));
				return true;
			}
		});
		
		ReflectionUtils.eachAnnotation(metaDataClass, Shows.class, new Predicate<Shows>() {
			public boolean apply(Shows input) {
				for (ShowFunction sf : input.value()) {
					shows.put(sf.name(), resolveShowFunction(sf, metaDataClass));
				}
				return true;
			}
		});
		
		return shows;
	}

	private Map<String, String> createListFunctions(final Class<?> metaDataClass) {
		final Map<String, String> lists = new HashMap<String, String>();
		
		ReflectionUtils.eachAnnotation(metaDataClass, ListFunction.class, new Predicate<ListFunction>() {
			public boolean apply(ListFunction input) {
				lists.put(input.name(), resolveListFunction(input, metaDataClass));
				return true;
			}
		});
		
		ReflectionUtils.eachAnnotation(metaDataClass, Lists.class, new Predicate<Lists>() {
			public boolean apply(Lists input) {
				for (ListFunction lf : input.value()) {
					lists.put(lf.name(), resolveListFunction(lf, metaDataClass));
				}
				return true;
			}
		});
		
		return lists;
	}

	private String resolveFilterFunction(Filter input,
			Class<?> metaDataClass) {
		if (input.file().length() > 0) {
			return loadFromFile(metaDataClass, input.file());
		}
		Assert.hasText(input.function(), "Filter must either have file or function value set");
		return input.function();
	}
	
	private String resolveListFunction(ListFunction input,
			Class<?> metaDataClass) {
		if (input.file().length() > 0) {
			return loadFromFile(metaDataClass, input.file());
		}
		Assert.hasText(input.function(), "ListFunction must either have file or function value set");
		return input.function();
	}

	private String resolveShowFunction(ShowFunction input,
			Class<?> metaDataClass) {
		if (input.file().length() > 0) {
			return loadFromFile(metaDataClass, input.file());
		}
		Assert.hasText(input.function(), "ShowFunction must either have file or function value set");
		return input.function();
	}
	
	private String loadFromFile(Class<?> metaDataClass, String file) {
		try {
			InputStream in = metaDataClass.getResourceAsStream(file);
			if (in == null) {
				throw new FileNotFoundException("Could not load file with path: " + file);
			}
			return IOUtils.toString(in, "UTF-8");
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}
}
