package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.C7nCertificationDTO
import io.choerodon.devops.app.service.impl.CertificationServiceImpl
import io.choerodon.devops.domain.application.repository.DevopsEnvUserPermissionRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.infra.dataobject.CertificationDO
import io.choerodon.devops.infra.dataobject.DevopsEnvironmentDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper
import io.choerodon.devops.infra.mapper.DevopsEnvironmentMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 *
 * @author zmf
 *
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(CertificationController)
@Stepwise
class CertificationControllerSpec extends Specification {
    private static final String BASE_URL = "/v1/projects/{project_id}/certifications"

    private static Long organizationId = 1L
    private static Long projectId = 1L

    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper
    @Autowired
    private DevopsEnvironmentMapper devopsEnvironmentMapper
    @Autowired
    private IamRepository iamRepository
    @Autowired
    private CertificationServiceImpl certificationService
    @Autowired
    private TestRestTemplate restTemplate

    private IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient)
    private DevopsEnvUserPermissionRepository mockDevopsEnvUserPermissionRepository = Mockito.mock(DevopsEnvUserPermissionRepository)

    @Shared
    private CertificationDO certificationDO = new CertificationDO()
    @Shared
    private DevopsEnvironmentDO devopsEnvironmentDO = new DevopsEnvironmentDO()
    @Shared
    private boolean isToInit = true
    @Shared
    private boolean isToClean = false

    def setup() {
        println('setup')
        if (isToInit) {
            DependencyInjectUtil.setAttribute(iamRepository, "iamServiceClient", iamServiceClient)
            DependencyInjectUtil.setAttribute(certificationService, "devopsEnvUserPermissionRepository", mockDevopsEnvUserPermissionRepository)

            // environment
            devopsEnvironmentDO.setName("env-test")
            devopsEnvironmentDO.setCode("env-test")
            devopsEnvironmentMapper.insert(devopsEnvironmentDO)

            // certification
            certificationDO.setOrganizationId(organizationId)
            certificationDO.setDomains("[\"aaa.c7n.wenqi.us\"]")
            certificationDO.setSkipCheckProjectPermission(Boolean.TRUE)
            certificationDO.setEnvId(devopsEnvironmentDO.getId())
            certificationDO.setName("cert-name")
            devopsCertificationMapper.insert(certificationDO)

            // mock iamServiceClient
            ProjectDO projectDO = new ProjectDO()
            projectDO.setId(projectId)
            projectDO.setOrganizationId(organizationId)
            ResponseEntity<ProjectDO> iamPro = new ResponseEntity<>(projectDO, HttpStatus.OK)
            Mockito.when(iamServiceClient.queryIamProject(Mockito.anyLong())).thenReturn(iamPro)
        }


    }

    def cleanup() {
        println('cleanup')
        if (isToClean) {
            devopsEnvironmentMapper.delete(devopsEnvironmentDO)
            devopsCertificationMapper.delete(certificationDO)
        }
    }

    def "Create"() {
        given: "插入数据"
        isToInit = false
        C7nCertificationDTO c7nCertificationDTO = new C7nCertificationDTO()
        c7nCertificationDTO.setEnvId(devopsEnvironmentDO.getId())
        c7nCertificationDTO.setEnvName("pro-cert-name")
        c7nCertificationDTO.setDomains(Arrays.asList("cd.as.aa.aa"))
        c7nCertificationDTO.setType("request")

        when: "创建证书"
        restTemplate.postForEntity(BASE_URL, c7nCertificationDTO, Object, projectId)

        then: "校验证书是否创建成功"
        devopsCertificationMapper.selectAll().size() == 1
    }

    def "ListByOptions"() {
    }

    def "GetActiveByDomain"() {
    }

    def "Delete"() {
//        given: "准备数据"
//
//        when: "删除证书"

    }

    def "CheckCertNameUniqueInEnv"() {
        given: "准备数据"
        def url = BASE_URL + "/unique?env_id={env_id}&cert_name={cert_name}"
        Map<String, Object> requestParams = new HashMap<>(2)
        requestParams.put("env_id", devopsEnvironmentDO.getId())
        requestParams.put("cert_name", certificationDO.getName())
        requestParams.put("project_id", projectId)

        when: "发送请求"
        def entity = restTemplate.getForEntity(url, Boolean, requestParams)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() == Boolean.FALSE

        and: "更改参数"
        requestParams.put("cert_name", certificationDO.getName() + "non")

        when: "再次请求"
        entity = restTemplate.getForEntity(url, Boolean, requestParams)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() == Boolean.TRUE
    }

    // 查询项目下有权限的组织层证书
    def "ListOrgCert"() {
        given: "设置删除数据"
        isToClean = true

        when: "发送请求"
        def entity = restTemplate.getForEntity(BASE_URL + "/list_org_cert", List, projectId)

        then: "校验结果"
        entity.getStatusCode().is2xxSuccessful()
        entity.getBody() != null
        !entity.getBody().isEmpty()
    }
}
