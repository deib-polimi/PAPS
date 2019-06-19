package it.polimi.ppap.service;

import it.polimi.deib.ppap.node.services.Service;

import java.util.HashSet;
import java.util.Set;

public class ServiceCatalog {

    private static Set<Service> serviceCatalog = new HashSet<>();

    public static Set<Service> getServiceCatalog() {
        return serviceCatalog;
    }

    public static void setServiceCatalog(Set<Service> serviceCatalog) {
        ServiceCatalog.serviceCatalog = serviceCatalog;
    }
}
