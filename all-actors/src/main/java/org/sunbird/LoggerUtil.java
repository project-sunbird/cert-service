package org.sunbird;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.request.RequestContext;

import java.util.List;
import java.util.Map;

public class LoggerUtil {

    private Logger logger;
    private String infoLevel = "INFO";
    private String debugLevel = "DEBUG";
    private String errorLevel = "ERROR";
    private String warnLevel = "WARN";
    private final ObjectMapper mapper = new ObjectMapper();

    public LoggerUtil(Class c) {
        logger = LoggerFactory.getLogger(c);
    }

    public void info(RequestContext requestContext, String message, Map<String, Object> object, List<Map<String, Object>> param) {
        if (requestContext != null) {
            requestContext.setLoggerLevel(infoLevel);
            logger.info(jsonMapper(requestContext, message, object, param));
        } else logger.info(message);
    }

    public void info(RequestContext requestContext, String message) {
        info(requestContext, message, null, null);
    }

    public void info(RequestContext requestContext, String message, Map<String, Object> object) {
        info(requestContext, message, object, null);
    }

    public void info(RequestContext requestContext, String message, List<Map<String, Object>> param) {
        info(requestContext, message, null, param);
    }

    public void debug(RequestContext requestContext, String message, Map<String, Object> object, List<Map<String, Object>> param) {
        if (isDebugEnabled(requestContext)) {
            requestContext.setLoggerLevel(debugLevel);
            logger.info(jsonMapper(requestContext, message, object, param));
        } else logger.debug(message);
    }

    public void debug(RequestContext requestContext, String message) {
        debug(requestContext, message, null, null);
    }

    public void debug(RequestContext requestContext, String message, Map<String, Object> object) {
        debug(requestContext, message, object, null);
    }

    public void debug(RequestContext requestContext, String message, List<Map<String, Object>> param) {
        debug(requestContext, message, null, param);
    }

    public void error(RequestContext requestContext, String message, Map<String, Object> object, List<Map<String, Object>> param, Throwable e) {
        if (requestContext != null) {
            requestContext.setLoggerLevel(errorLevel);
            logger.error(jsonMapper(requestContext, message, object, param), e);
        } else logger.error(message, e);
    }

    public void error(RequestContext requestContext, String message, Throwable e) {
        error(requestContext, message, null, null, e);
    }

    public void error(RequestContext requestContext, String message, Map<String, Object> object, Throwable e) {
        error(requestContext, message, object, null, e);
    }

    public void error(RequestContext requestContext, String message, List<Map<String, Object>> param, Throwable e) {
        error(requestContext, message, null, param, e);
    }

    public void warn(RequestContext requestContext, String message, Map<String, Object> object, List<Map<String, Object>> param, Throwable e) {
        if (requestContext != null) {
            requestContext.setLoggerLevel(warnLevel);
            logger.warn((jsonMapper(requestContext, message, object, param)), e);
        } else logger.warn(message, e);
    }

    public void warn(RequestContext requestContext, String message, Throwable e) {
        warn(requestContext, message, null, null, e);
    }

    public void warn(RequestContext requestContext, String message, Map<String, Object> object, Throwable e) {
        warn(requestContext, message, object, null, e);
    }

    public void warn(RequestContext requestContext, String message, List<Map<String, Object>> param, Throwable e) {
        warn(requestContext, message, null, param, e);
    }

    private static boolean isDebugEnabled(RequestContext requestContext) {
        return (null != requestContext && StringUtils.equalsIgnoreCase("true", requestContext.getDebugEnabled()));
    }

    private String jsonMapper(RequestContext requestContext, String message, Map<String, Object> object, List<Map<String, Object>> param) {
        try {
            return mapper.writeValueAsString(new CustomLogFormat(requestContext, message, object, param).getEventMap());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
