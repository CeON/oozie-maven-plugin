package pl.edu.icm.maven.oozie.plugin;

public class OoziePluginConstants {

    public static final String MAVEN_RESOURCES_PLUGIN_VERSION = "2.6";
    public static final String MAVEN_FAILSAFE_PLUGIN_VERSION = "2.14.1";
    public static final String MAVEN_CLEAN_PLUGIN_VERSION = "2.5";
    public static final String MAVEN_DEPENDENCY_PLUGIN_VERSION = "2.7";
    public static final String MAVEN_ASSEMBLY_PLUGIN_VERSION = "2.3";
    public static final String HDFS_URI = "nameNode";
    public static final String HDFS_WF_WORKING_DIR = "hdfsWfWorkingDir";
    public static final String WF_DIR = "wfDir";
    public static final String HDFS_USER_NAME = "hdfsUserName";
    public static final String OOZIE_WF_CLASSIFIER = "oozie-wf";
    public static final String OOZIE_WF_PREPARE_PACKAGE_DIR = "oozie-wf";

    public static final String IT_ENV_PLACEHOLDER = "${IT.env}";
    public static final String IT_ENV_PROPERTIES_LOCATION = "configIT/env/IT-env-"
			+ IT_ENV_PLACEHOLDER + ".properties";

    public static final String PLACEHOLDER_PREFIX_NAME = "plcaeholder.prefix";
    public static final String PLACEHOLDER_SUFFIX_NAME = "placeholder.suffix";
    public static final String PLACEHOLDER_VALUE_SEPARATOR_NAME = "placeholder.valueSeparator";
    public static final String PLACEHOLDER_IGNORE_UNRESOLVABLE_PLACEHOLDERS_NAME = "placeholder.ignoreUnresolvablePlaceholders";

    public static final String PLACEHOLDER_PREFIX_DEFAULT = "${";
    public static final String PLACEHOLDER_SUFFIX_DEFAULT = "}";
    public static final String PLACEHOLDER_VALUE_SEPARATOR_DEFAULT = ":";
    public static final String PLACEHOLDER_IGNORE_UNRESOLVABLE_PLACEHOLDERS_DEFAULT = "false";
}
