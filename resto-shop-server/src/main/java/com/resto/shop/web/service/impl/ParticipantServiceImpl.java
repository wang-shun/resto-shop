package com.resto.shop.web.service.impl;

import cn.restoplus.rpc.server.RpcService;
import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.shop.web.dao.ParticipantMapper;
import com.resto.shop.web.model.Participant;
import com.resto.shop.web.service.ParticipantService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by carl on 2017/9/25.
 */
@RpcService
public class ParticipantServiceImpl extends GenericServiceImpl<Participant, Long> implements ParticipantService {

    @Resource
    private ParticipantMapper participantMapper;

    @Override
    public GenericDao<Participant, Long> getDao() {
        return participantMapper;
    }

    @Override
    public List<Participant> selectCustomerListByGroupIdOrderId(String groupId, String orderId) {
        return participantMapper.selectCustomerListByGroupIdOrderId(groupId, orderId);
    }

    @Override
    public Participant selectByOrderIdCustomerId(String orderId, String customerId) {
        return participantMapper.selectByOrderIdCustomerId(orderId, customerId);
    }
}
