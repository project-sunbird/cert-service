package org.sunbird.message;

/**
 * This interface will hold all the response key and message
 *
 * @author Amit Kumar
 */
public interface IResponseMessage {

  String INVALID_REQUESTED_DATA = "INVALID_REQUESTED_DATA";
  String INVALID_OPERATION_NAME = "INVALID_OPERATION_NAME";
  String INTERNAL_ERROR = "INTERNAL_ERROR";
  String MANDATORY_PARAMETER_MISSING = "Mandatory parameter {0} is missing.";
  String INVALID_PARAM_VALUE = "Invalid value {0} for parameter {1}.";
  String SERVICE_UNAVAILABLE = "SERVICE UNAVAILABLE";
}
