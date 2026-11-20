package io.joshuasalcedo.fx.ui.application.search.service;


public interface Query<DATA, PARAMS>{
        DATA query(PARAMS params);
}
