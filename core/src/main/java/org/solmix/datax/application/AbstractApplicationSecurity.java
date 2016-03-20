package org.solmix.datax.application;

import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataService;


public abstract class AbstractApplicationSecurity implements ApplicationSecurity
{


    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public boolean isExclude(String operationid) {
        return false;
    }

    @Override
    public DSResponse excute(DataService ds, DSRequest request) throws DSCallException {
        return ds.execute(request);
    }

}
