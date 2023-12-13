find_package(PkgConfig)
pkg_check_modules(PC_OPENLIBM QUIET openlibm)

add_library(MathLib::MathLib INTERFACE IMPORTED)
set_property(TARGET MathLib::MathLib PROPERTY INTERFACE_LINK_LIBRARIES "${PC_OPENLIBM_LIBRARIES}")
set_property(TARGET MathLib::MathLib PROPERTY INTERFACE_INCLUDE_DIRECTORIES "${PC_OPENLIBM_INCLUDE_DIRS}")
