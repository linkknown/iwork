package com.linkknown.iwork;

public class Constants {

    public static final String STRING_PREFIX = "str_";
    public static final String MULTI_PREFIX = "multi_";
    public static final String NUMBER_PREFIX = "number_";
    public static final String COMPLEX_PREFIX = "complex_";
    public static final String BOOL_PREFIX = "bool_";
    public static final String FOREACH_PREFIX = "iter_";


    public static final String NODE_TYPE_WORK_SUB = "work_sub";
    public static final String NODE_TYPE_WORK_START = "work_start";

    public static final String[] FORBIDDEN_WORK_NAMES = new String[]{"WORK", "RESOURCE", "GLOBAL"};

    public static final String LOG_LEVEL_INFO = "INFO";
    public static final String LOG_LEVEL_ERROR = "ERROR";
    public static final String LOG_LEVEL_SUCCESS = "SUCCESS";

    public static final int PARENT_STEP_ID_FOR_START_END = -1;

    public static final String HTTP_REQUEST_OBJECT = "____request";

    public static final String TRACKING_ID = "__trackingId";

    public static final String FILTER_TRACKING_ID_STACK = "filter__trackingId_stack";
    public static final String DO_ERROR_FILTER = "__doErrorFilter__";

}
