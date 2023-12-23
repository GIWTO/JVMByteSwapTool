package w.util;

import fi.iki.elonen.NanoWSD;
import w.core.MethodId;
import w.core.Retransformer;
import w.util.model.ClassFileTransformerWrapper;

import java.lang.instrument.ClassFileTransformer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Frank
 * @date 2023/12/21 11:43
 */
public class RequestUtils {
    private final static ThreadLocal<NanoWSD.WebSocket> socketCtx = new ThreadLocal<>();
    private final static ThreadLocal<String> traceIdCtx = new ThreadLocal<>();

    public final static Map<String, NanoWSD.WebSocket> traceId2Ws = new ConcurrentHashMap<>();

    public static Map<String, Map<String, List<ClassFileTransformerWrapper>>> activeTransformer = new HashMap<>();


    private static ThreadLocal<Map<String, Set<ClassLoader>>> classToLoader = ThreadLocal.withInitial(HashMap::new);

    public final static Map<String, Map<MethodId, Retransformer>> traceId2MethodId2Trans = new ConcurrentHashMap<>();

    public static void initRequestCtx(NanoWSD.WebSocket ws, String traceId) {
        socketCtx.set(ws);
        traceIdCtx.set(traceId);
        if (traceId != null && ws != null) {
            traceId2Ws.put(traceId, ws);
        }
    }

    public static void clearRequestCtx() {
        socketCtx.remove();
        traceIdCtx.remove();
    }

    public static void addTransformer(String className, String loader, ClassFileTransformer transformer) {
        activeTransformer.computeIfAbsent(className, k -> new HashMap<>())
                .computeIfAbsent(loader, k -> new ArrayList<>())
                .add(new ClassFileTransformerWrapper(transformer));
    }

    public static void updateTransformerStatus(String className, String loader, ClassFileTransformer transformer, int status) {
        activeTransformer.computeIfAbsent(className, k -> new HashMap<>())
                .computeIfAbsent(loader, k -> new ArrayList<>())
                .stream().filter(it -> it.getTransformer() == transformer)
                .findFirst().ifPresent(it -> it.setStatus(status));
    }


    public String getTraceId() {
        return traceIdCtx.get();
    }

    public static NanoWSD.WebSocket getCurWs() {
        return socketCtx.get();
    }

    public static void fillCurThread(String traceId) {
        initRequestCtx(traceId2Ws.get(traceId), traceId);
    }

    public static String getCurTraceId() {
        return traceIdCtx.get();
    }

    public static NanoWSD.WebSocket getWsByTraceId(String traceId) {
        return traceId2Ws.get(traceId);
    }

}
