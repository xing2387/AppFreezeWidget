package xing.appwidget.bean

import java.util.*

data class PackageFilterParam(var system: Boolean = false, var user: Boolean = false,
                              var enabled: Boolean = true, var disabled: Boolean = false,
                              var labels: List<String> = Collections.emptyList(), var initWithGrid: Boolean)