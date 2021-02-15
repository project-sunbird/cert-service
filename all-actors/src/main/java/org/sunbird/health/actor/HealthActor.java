/**
 * 
 */
package org.sunbird.health.actor;

import org.sunbird.BaseActor;
import org.sunbird.LoggerUtil;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
/**
 * @author manzarul
 *
 */
public class HealthActor extends BaseActor{
	public LoggerUtil logger = new LoggerUtil(this.getClass());

	@Override
	public void onReceive(Request request) throws Throwable {
		logger.customLogFormat(request.getRequestContext(), request.getId(), "HealthActor: HealthActor called for operation: status check", null, null, null);
		logger.info(request.getRequestContext(), "HealthActor: HealthActor called for operation: status check");
		Response response = new Response();
		response.getResult().put("response", "Success");
		sender().tell(response, getSelf());
	}

}
