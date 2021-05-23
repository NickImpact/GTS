package net.impactdev.gts.api.exceptions

class LackingServiceException(val lacking: Class<*>) : RuntimeException(lacking.canonicalName)