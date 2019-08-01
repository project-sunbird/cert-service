package controllers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import javax.inject.Inject;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.Application;
import org.sunbird.BaseException;
import org.sunbird.message.Localizer;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import utils.JsonKey;
import utils.RequestMapper;

/**
 * This controller we can use for writing some common method to handel api request.
 * CompletableFuture: A Future that may be explicitly completed (setting its value and status), and
 * may be used as a CompletionStage, supporting dependent functions and actions that trigger upon
 * its completion. CompletionStage: A stage of a possibly asynchronous computation, that performs an
 * action or computes a value when another CompletionStage completes
 *
 * @author Anmol
 */
public class BaseController extends Controller { /**
     * We injected HttpExecutionContext to decrease the response time of APIs.
     */
    @Inject
    private HttpExecutionContext httpExecutionContext;
    protected static Localizer localizerObject = Localizer.getInstance();

    /**
     * This is temporary method we use get dummyresponse to check APIs.
     *
     * @return string
     */
    public String getDummyResponse() {
        startTrace("getDummyResponse");
        String dummyResponse =
                "{\"id\":\"api.user.200ok\",\"ver\":\"v1\",\"ts\":\"2019-01-17 16:53:26:286+0530\",\"params\":{\"resmsgid\":null,\"msgid\":\"8e27cbf5-e299-43b0-bca7-8347f7ejk5abcf\",\"err\":null,\"status\":\"success\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"response\":{\"response\":\"SUCCESS\",\"errors\":[]}}}";
        endTrace("getDummyResponse");
        return dummyResponse;
    }

    public CompletionStage<Result> handelRequest() {
        Http.RequestBody requestBody = request().body();
        startTrace("handelRequest");
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete(getDummyResponse());
        endTrace("handelRequest");
        return future.thenApplyAsync(Results::ok, httpExecutionContext.current());
    }

    /**
     * This method will return the current timestamp.
     *
     * @return long
     */
    public long getTimeStamp() {
        return System.currentTimeMillis();
    }

    /**
     * This method we used to print the logs of starting time of methods
     *
     * @param tag
     */
    public void startTrace(String tag) {
		/*
		 * ProjectLogger.log( String.format("%s:%s:started at %s",
		 * this.getClass().getSimpleName(), tag, getTimeStamp()),
		 * LoggerEnum.DEBUG.name());
		 */
    }

    /**
     * This method we used to print the logs of ending time of methods
     *
     * @param tag
     */
    public void endTrace(String tag) {
		/*
		 * ProjectLogger.log( String.format("%s:%s:ended at %s",
		 * this.getClass().getSimpleName(), tag, getTimeStamp()),
		 * LoggerEnum.DEBUG.name());
		 */
    }

    protected ActorRef getActorRef(String operation) throws BaseException {
        return Application.getInstance().getActorRef(operation);
    }


    /**
     * this method will take play.mv.http request and a validation function and lastly operation(Actor operation)
     * it will map the request to our sunbird Request class.
     *
     * @param request
     * @param function
     * @param operation
     * @return
     */
//    public CompletionStage<Result> handleRequest(play.mvc.Http.Request request, Function function, String operation) {
//
//        return handleRequest(req, function, operation);
//    }


    /**
     * this method will take play.mv.http request and a validation function and lastly operation(Actor operation)
     * this method is validating the request and ,
     * it will map the request to our sunbird Request class and make a call to requestHandler which is internally calling ask to actor
     * this method is used to handle all the request type which has requestBody
     *
     * @param req
     * @param validatorFunction
     * @param operation
     * @return
     */
    public CompletionStage<Result> handleRequest(play.mvc.Http.Request req, Function validatorFunction, String operation) {
        try {
        	Request request = new Request();
        	if(req.body() != null && req.body().asJson() != null) {
                request = (Request) RequestMapper.mapRequest(req, Request.class);
        	 }
            if (validatorFunction != null) {
                validatorFunction.apply(request);
            }
            return new RequestHandler().handleRequest(request, httpExecutionContext, operation);
        }  catch (BaseException ex) {
            return RequestHandler.handleFailureResponse(ex, httpExecutionContext);
        } catch (Exception ex) {
            return RequestHandler.handleFailureResponse(ex, httpExecutionContext);
        }

    }

    /**
     * this method is used to handle the only GET requests.
     *
     * @param req
     * @param operation
     * @return
     */
    public CompletionStage<Result> handleRequest(Request req, String operation) {
        try {
            return new RequestHandler().handleRequest(req, httpExecutionContext, operation);
        }  catch (BaseException ex) {
            return RequestHandler.handleFailureResponse(ex, httpExecutionContext);
        } catch (Exception ex) {
            return RequestHandler.handleFailureResponse(ex, httpExecutionContext);
        }

    }


    /**
     * This method is responsible to convert Response object into json
     *
     * @param response
     * @return string
     */
    public static String jsonifyResponseObject(Response response) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            return JsonKey.EMPTY_STRING;
        }
    }

    /**
     * This method is used specifically to handel Log Apis request this will set log levels and then
     * return the CompletionStage of Result
     *
     * @return
     */
    public CompletionStage<Result> handleLogRequest() {
        startTrace("handleLogRequest");
        Response response = new Response();
        Request request = null;
        try {
            request = (Request) RequestMapper.mapRequest(request(), Request.class);
        } catch (Exception ex) {
           // ProjectLogger.log(String.format("%s:%s:exception occurred in mapping request", this.getClass().getSimpleName(), "handleLogRequest"), LoggerEnum.ERROR.name());
            return RequestHandler.handleFailureResponse(ex, httpExecutionContext);
        }

		/*
		 * if (LogValidator.isLogParamsPresent(request)) { if
		 * (LogValidator.isValidLogLevelPresent((String)
		 * request.get(JsonKey.LOG_LEVEL))) {
		 * ProjectLogger.setUserOrgServiceProjectLogger( (String)
		 * request.get(JsonKey.LOG_LEVEL)); response.put(JsonKey.ERROR, false);
		 * response.put( JsonKey.MESSAGE, "Log Level successfully set to " +
		 * request.get(JsonKey.LOG_LEVEL)); } else { List<Enum> supportedLogLevelsValues
		 * = new ArrayList<>(EnumSet.allOf(LoggerEnum.class));
		 * response.put(JsonKey.ERROR, true); response.put( JsonKey.MESSAGE,
		 * "Valid Log Levels are " + Arrays.asList(supportedLogLevelsValues.toArray()));
		 * } } else { response.put(JsonKey.ERROR, true); response.put( JsonKey.MESSAGE,
		 * "Missing Mandatory Request Param " + JsonKey.LOG_LEVEL); }
		 */
        return RequestHandler.handleSuccessResponse(response, httpExecutionContext);
    }
}
