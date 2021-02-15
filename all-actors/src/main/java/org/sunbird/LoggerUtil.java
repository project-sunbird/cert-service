package org.sunbird;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

import net.logstash.logback.marker.Markers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.request.RequestContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoggerUtil {

    private Logger logger;

    public  LoggerUtil(Class c) {
        logger = LoggerFactory.getLogger(c);
    }

    public void info(RequestContext requestContext, String message, Object data) {
        if(null != requestContext) {
            logger.info(Markers.appendEntries(requestContext.getContextMap()), message, data);
        } else {
            logger.info(message, data);
        }
    }

    public void info(RequestContext requestContext, String message) {
        info(requestContext, message, null);
    }

    public void info(Map<String, Object> eventMap) {
        logger.info("", keyValue("CustomLog", eventMap));
    }

    public void error(RequestContext requestContext, String message, Throwable e) {
        if(null != requestContext) {
            logger.error(Markers.appendEntries(requestContext.getContextMap()) ,message, e);
        } else {
            logger.error(message, e);
        }
    }

    public void warn(RequestContext requestContext, String message, Throwable e) {
        if(null != requestContext) {
            logger.warn(Markers.appendEntries(requestContext.getContextMap()), message, e);
        } else {
            logger.warn(message, e);
        }
    }

    public void debug(RequestContext requestContext, String message, Object data) {
        if(isDebugEnabled(requestContext)) {
            logger.info(Markers.appendEntries(requestContext.getContextMap()), message, data);
        } else {
            logger.debug(message, data);
        }
    }

    public void debug(RequestContext requestContext, String message) {debug(requestContext, message, null);}

    private static boolean isDebugEnabled(RequestContext requestContext) {
        return (null != requestContext && StringUtils.equalsIgnoreCase("true", requestContext.getDebugEnabled()));
    }

    public void customLogFormat(RequestContext requestContext, String requestId, String msg, Map<String, Object> actor, Map<String, Object> object, Map<String, Object> params) {
        Map<String, Object> edata = new HashMap<>();
        Map<String, Object> eventMap = new HashMap<>();
        edata.put("type", "system");
        edata.put("level", "INFO");
        edata.put("requestid", requestId);
        edata.put("message", msg);
        if(params != null)
            edata.put("params", params);


        eventMap.putAll(new HashMap<String, Object>(){{
            put("eid", "LOG");
            put("ets", System.currentTimeMillis());
            put("ver", "3.0");
            put("mid", "LOG:" + UUID.randomUUID().toString());

            put("context", requestContext);
            put("actor", actor);
            if(object != null)
                put("object", object);
            put("edata", edata);
        }});
        info(eventMap);
    }

}
