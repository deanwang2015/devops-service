package io.choerodon.devops.infra.persistence.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsGitlabPipelineE;
import io.choerodon.devops.domain.application.repository.DevopsGitlabPipelineRepository;
import io.choerodon.devops.infra.dataobject.DevopsGitlabPipelineDO;
import io.choerodon.devops.infra.mapper.DevopsGitlabPipelineMapper;

@Service
public class DevopsGitlabPipelineRepositoryImpl implements DevopsGitlabPipelineRepository {


    @Autowired
    private DevopsGitlabPipelineMapper devopsGitlabPipelineMapper;


    @Override
    public void create(DevopsGitlabPipelineE devopsGitlabPipelineE) {
        DevopsGitlabPipelineDO devopsGitlabPipelineDO = ConvertHelper.convert(devopsGitlabPipelineE, DevopsGitlabPipelineDO.class);
        if (devopsGitlabPipelineMapper.insert(devopsGitlabPipelineDO) != 1) {
            throw new CommonException("error.gitlab.pipeline.create");
        }
    }

    @Override
    public DevopsGitlabPipelineE queryByGitlabPipelineId(Long id) {
        DevopsGitlabPipelineDO devopsGitlabPipelineDO = new DevopsGitlabPipelineDO();
        devopsGitlabPipelineDO.setPipelineId(id);
        return ConvertHelper.convert(devopsGitlabPipelineMapper.selectOne(devopsGitlabPipelineDO), DevopsGitlabPipelineE.class);
    }

    @Override
    public void update(DevopsGitlabPipelineE devopsGitlabPipelineE) {
        DevopsGitlabPipelineDO devopsGitlabPipelineDO = devopsGitlabPipelineMapper.selectByPrimaryKey(devopsGitlabPipelineE.getId());
        DevopsGitlabPipelineDO updateDevopsGitlabPipelineDO = ConvertHelper.convert(devopsGitlabPipelineE, DevopsGitlabPipelineDO.class);
        updateDevopsGitlabPipelineDO.setObjectVersionNumber(devopsGitlabPipelineDO.getObjectVersionNumber());
        if (devopsGitlabPipelineMapper.updateByPrimaryKeySelective(updateDevopsGitlabPipelineDO) != 1) {
            throw new CommonException("error.gitlab.pipeline.update");
        }
    }

    @Override
    public DevopsGitlabPipelineE queryByCommitId(Long commitId) {
        DevopsGitlabPipelineDO devopsGitlabPipelineDO = new DevopsGitlabPipelineDO();
        devopsGitlabPipelineDO.setCommitId(commitId);
        return ConvertHelper.convert(devopsGitlabPipelineMapper.selectOne(devopsGitlabPipelineDO), DevopsGitlabPipelineE.class);
    }

    @Override
    public List<DevopsGitlabPipelineDO> pipelineTime(Long appId, Date startTime, Date endTime) {
        return devopsGitlabPipelineMapper.listDevopsGitlabPipeline(appId, new java.sql.Date(startTime.getTime()), new java.sql.Date(endTime.getTime()));
    }
}
