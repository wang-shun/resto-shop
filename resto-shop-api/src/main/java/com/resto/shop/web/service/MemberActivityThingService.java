package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.MemberActivityThing;

public interface MemberActivityThingService extends GenericService<MemberActivityThing, Integer> {

    MemberActivityThing selectByTelephone(String telephone);

}
