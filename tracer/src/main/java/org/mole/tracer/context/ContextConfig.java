package org.mole.tracer.context;

import org.k3a.observer.impl.FileObserver;
import org.mole.tracer.dto.P;
import org.mole.tracer.utils.SimpleLoggerManager;
import org.mole.tracer.utils.SimpleStringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.*;

import static org.mole.tracer.context.ContextConfig.ConfigKey.*;

/**
 * Created by k3a
 * on 19-1-3  下午9:05
 */
public class ContextConfig {

    public static final Set<String> CONFIG_VALUES = new HashSet<>();

    static {
        for (ConfigValue value : ConfigValue.values()) {
            CONFIG_VALUES.add(value.name());
        }
    }

    // simple access, do not change those value outside this class!
    public int _rate;
    public boolean _async;
    public String _helperClass;

    private String _separator;
    private String _argsSeparator;

    private Set<String> watchedMethods;
    private List<String> extra;
    //key -> ,value -> left trace class name,right span class name
    public Map<String, P<String, String>> genS;
    //  1 -> log 2 -> kcp  3 -> log and kcp
    public int _recordMode;

    public ContextConfig(Path path) throws InterruptedException {
        Objects.requireNonNull(path, "dynamically can not be null");
        updateProps(path);
        //hot swap sample rate _separator
        FileObserver.get()
                .register(path)
                .onDelete(this::swap)
                .onModify(this::swap)
                .onCreate(this::swap)
                .start();
    }

    /**
     *
     */
    private void updateProps(Path path) {
        final Map<String, Object> config = swap(path);

        //log dir
        final String logDir = getStringFromMap(config, tracer.name(), watcher.name(), recordMode.name(), log.name(), dir.name());
        if (SimpleStringUtils.isNotBlank(logDir)) {
            System.setProperty("log.dir", logDir);
        } else {
            System.setProperty("log.dir", System.getProperty("user.dir"));
        }
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        final Map<String, Object> methods = getMapFromMap(config, tracer.name(), watcher.name(), names.name());
        //noinspection unchecked
        if (methods == null || methods.isEmpty() || (this.watchedMethods = methods.keySet()).isEmpty()) {
            throw new IllegalArgumentException("watchedMethods can not be null");
        }

        //noinspection unchecked
        final List<String> extras = getListFromMap(config, tracer.name(), watcher.name(), recordField.name());
        if (extras != null) {
            final Iterator<String> iterator = extras.iterator();
            while (iterator.hasNext()) {
                final String next = iterator.next();
                if (!CONFIG_VALUES.contains(next)) {
                    iterator.remove();
                    SimpleLoggerManager.error("discard not matched recordFields config:" + next);
                }
            }
        }
        this.extra = extras;

        // trace span generator
        final Map<String, P<String, String>> genS = new HashMap<>();
        for (Map.Entry<String, Object> entry : methods.entrySet()) {
            //noinspection unchecked
            final Map<String, String> methodConf = (Map<String, String>) entry.getValue();
            genS.put(entry.getKey(), new P<String, String>(methodConf.get(traceClass.name()), methodConf.get(spanClass.name())));
        }
        this.genS = genS;

        //_recordMode
        //todo handle kcp argus
        final Map<String, Object> recordMode = getMapFromMap(config, tracer.name(), watcher.name(), ConfigKey.recordMode.name());
        if (recordMode == null || !recordMode.containsKey(log.name())) {
            throw new IllegalArgumentException("recordMode can not be null");
        } else if (recordMode.containsKey(kcp.name())) {
            this._recordMode = 3;//open kcp and log
        } else {
            this._recordMode = 1;//open log
        }

        //_separator
        this._separator = getStringFromMap(config, tracer.name(), watcher.name(), separator.name());
        this._argsSeparator = getStringFromMap(config, tracer.name(), watcher.name(), argsSeparator.name());
    }

    /**
     * reload when config file changed
     */
    private Map<String, Object> swap(Path path) {
        final Map<String, Object> config = load(path);

        //rate
        final String _rate = getStringFromMap(config, tracer.name(), sampler.name(), rate.name());
        if (SimpleStringUtils.isBlank(_rate)) {
            this._rate = 100;
        } else {
            this._rate = Integer.parseInt(_rate);
        }

        //sync or _async record
        this._async = Boolean.getBoolean(getStringFromMap(config, tracer.name(), watcher.name(), async.name()));

        //helper class
        this._helperClass = getStringFromMap(config, tracer.name(), watcher.name(), helperClass.name());

        return config;
    }

    private Map<String, Object> load(Path path) {
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            final Yaml yaml = new Yaml();
            //noinspection unchecked
            return yaml.loadAs(fis, Map.class);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static Object getFromMap(Map map, String... key) {
        Map tmp = map;
        for (int i = 0; i < key.length; i++) {
            String s = key[i];
            final Object o = tmp.get(s);
            if (o instanceof Map) {
                tmp = (Map) o;
            } else if (i == key.length - 1) {
                return o;
            } else {
                return null;
            }
        }
        return tmp;
    }

    @SuppressWarnings("WeakerAccess")
    public static String getStringFromMap(Map map, String... key) {
        final Object tmp = getFromMap(map, key);
        return tmp == null ? null : tmp.toString();
    }

    @SuppressWarnings("WeakerAccess")
    public static Map<String, Object> getMapFromMap(Map map, String... key) {
        final Object tmp = getFromMap(map, key);
        if (tmp instanceof Map) {
            //noinspection unchecked
            return (Map) tmp;
        } else if (tmp == null) {
            return null;
        } else {
            throw new IllegalStateException("not map:" + SimpleStringUtils.join(".", key));
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static List getListFromMap(Map map, String... key) {
        final Object tmp = getFromMap(map, key);
        if (tmp instanceof List) {
            return (List) tmp;
        } else if (tmp == null) {
            return null;
        } else {
            throw new IllegalStateException("not list:" + SimpleStringUtils.join(".", key));
        }
    }

    public List<String> getExtra() {
        return extra;
    }

    public Set<String> getWatchedMethods() {
        return watchedMethods;
    }

    public Map<String, P<String, String>> getMethodGen() {
        return genS;
    }

    public int getRecordMode() {
        return _recordMode;
    }

    public String get_separator() {
        return _separator;
    }

    public String get_argsSeparator() {
        return _argsSeparator;
    }

    public enum ConfigKey {
        tracer, sampler, rate, watcher, async, separator, names, traceClass,
        spanClass, annotation, method, trace, span, recordField, helperClass,
        recordMode, log, dir, kcp, argsSeparator
    }

    public enum ConfigValue {
        methodName, methodDesc, currentThread, currentTimeMills, traceId, spanId, duration, methodArgs
    }

}
