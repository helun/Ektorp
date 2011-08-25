package org.ektorp.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.ektorp.util.Assert;

/**
 * Representation of a CouchDb design document.
 * 
 * Design documents can contain fields currently not handled by Ektorp, such as update handlers and validators. These
 * fields are still accessible through the methods getAnonymous() and setAnonymous(String key, Object value)
 * 
 * @author henrik lundgren
 * 
 */
public class DesignDocument extends OpenCouchDbDocument {

    private static final long serialVersionUID = 727813829995624926L;

    public final static String ID_PREFIX = "_design/";
    private final static String DEFAULT_LANGUAGE = "javascript";
    public static final String AUTO_UPDATE_VIEW_ON_CHANGE = "org.ektorp.support.AutoUpdateViewOnChange";
    public static final String UPDATE_ON_DIFF = "org.ektorp.support.UpdateDesignDocOnDiff";

    private Map<String, View> views;
    private Map<String, String> lists;
    private Map<String, String> shows;
    private Map<String, String> updateHandlers;

    private String language = DEFAULT_LANGUAGE;

    private Map<String, String> filters;

    public DesignDocument() {
    }

    public DesignDocument(String id) {
        setId(id);
    }

    @JsonProperty
    public Map<String, View> getViews() {
        return Collections.unmodifiableMap(views());
    }

    @JsonProperty
    public Map<String, String> getLists() {
        return Collections.unmodifiableMap(lists());
    }

    @JsonProperty
    public Map<String, String> getShows() {
        return Collections.unmodifiableMap(shows());
    }

    @JsonProperty
    public Map<String, String> getFilters() {
        return Collections.unmodifiableMap(filters());
    }

    @JsonProperty
    public Map<String, String> getUpdates() {
        return Collections.unmodifiableMap(updates());
    }

    private Map<String, String> lists() {
        if (lists == null) {
            lists = new HashMap<String, String>();
        }
        return lists;
    }

    private Map<String, String> shows() {
        if (shows == null) {
            shows = new HashMap<String, String>();
        }
        return shows;
    }

    private Map<String, View> views() {
        if (views == null) {
            views = new HashMap<String, View>();
        }
        return views;
    }

    private Map<String, String> filters() {
        if (filters == null) {
            filters = new HashMap<String, String>();
        }
        return filters;
    }

    private Map<String, String> updates() {
        if (updateHandlers == null) {
            updateHandlers = new HashMap<String, String>();
        }
        return updateHandlers;
    }

    @JsonProperty
    void setViews(Map<String, View> views) {
        this.views = views;
    }

    @JsonProperty
    void setShows(Map<String, String> shows) {
        this.shows = shows;
    }

    @JsonProperty
    void setLists(Map<String, String> lists) {
        this.lists = lists;
    }

    @JsonProperty
    void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    @JsonProperty
    void setUpdates(Map<String, String> updateHandlers) {
        this.updateHandlers = updateHandlers;
    }

    @JsonProperty
    public String getLanguage() {
        return language;
    }

    @JsonProperty
    public void setLanguage(String s) {
        this.language = s;
    }

    public boolean containsView(String name) {
        return views().containsKey(name);
    }

    public View get(String viewName) {
        return views().get(viewName);
    }

    public void addView(String name, View v) {
        views().put(name, v);
    }

    public void removeView(String name) {
        views().remove(name);
    }

    public void addListFunction(String name, String function) {
        lists().put(name, function);
    }

    public void removeListFunction(String name) {
        lists().remove(name);
    }

    public void addShowFunction(String name, String function) {
        shows().put(name, function);
    }

    public void removeShowFunction(String name) {
        shows().remove(name);
    }

    public void addFilter(String name, String function) {
        filters().put(name, function);
    }

    public void removeFilter(String name) {
        filters().remove(name);
    }

    public boolean mergeWith(DesignDocument dd) {
        boolean updateOnDiff = updateOnDiff();
        boolean changed = mergeViews(dd.views(), updateOnDiff);
        changed = mergeFunctions(lists(), dd.lists(), updateOnDiff) || changed;
        changed = mergeFunctions(shows(), dd.shows(), updateOnDiff) || changed;
        changed = mergeFunctions(filters(), dd.filters(), updateOnDiff) || changed;
        changed = mergeFunctions(updates(), dd.updates(), updateOnDiff) || changed;
        return changed;
    }

    private boolean mergeFunctions(Map<String, String> existing, Map<String, String> mergeFunctions,
            boolean updateOnDiff) {
        boolean changed = false;
        for (Map.Entry<String, String> e : mergeFunctions.entrySet()) {
            String name = e.getKey();
            String func = e.getValue();
            if (!existing.containsKey(name)) {
                existing.put(name, func);
                changed = true;
            } else if (updateOnDiff) {
                String existingFunc = existing.get(name);
                if (!existingFunc.equals(func)) {
                    existing.put(name, func);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean mergeViews(Map<String, View> mergeViews, boolean updateOnDiff) {
        boolean changed = false;
        for (Map.Entry<String, DesignDocument.View> e : mergeViews.entrySet()) {
            String name = e.getKey();
            DesignDocument.View candidate = e.getValue();
            if (!containsView(name)) {
                addView(name, candidate);
                changed = true;
            } else if (updateOnDiff) {
                DesignDocument.View existing = get(name);
                if (!existing.equals(candidate)) {
                    addView(name, candidate);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean updateOnDiff() {
        return Boolean.getBoolean(AUTO_UPDATE_VIEW_ON_CHANGE) || Boolean.getBoolean(UPDATE_ON_DIFF);
    }

    /**
     * Definition of a view in a design document.
     * 
     * @author henrik lundgren
     * 
     */
    @JsonSerialize(include = Inclusion.NON_NULL)
    public static class View {
        @JsonProperty
        private String map;
        @JsonProperty
        private String reduce;

        public View() {
        }

        public static View of(org.ektorp.support.View v) {
            return v.reduce().length() == 0 ?
                    new DesignDocument.View(v.map()) :
                    new DesignDocument.View(v.map(), v.reduce());
        }

        public View(String map) {
            Assert.hasText(map, "the map function may not be null or empty");
            this.map = map;
        }

        public View(String map, String reduce) {
            this(map);
            this.reduce = reduce;
        }

        public String getMap() {
            return map;
        }

        public void setMap(String map) {
            this.map = map;
        }

        public String getReduce() {
            return reduce;
        }

        public void setReduce(String reduce) {
            this.reduce = reduce;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((map == null) ? 0 : map.hashCode());
            result = prime * result
                    + ((reduce == null) ? 0 : reduce.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            View other = (View) obj;
            if (map == null) {
                if (other.map != null)
                    return false;
            } else if (!map.equals(other.map))
                return false;
            if (reduce == null) {
                if (other.reduce != null)
                    return false;
            } else if (!reduce.equals(other.reduce))
                return false;
            return true;
        }

    }

}
