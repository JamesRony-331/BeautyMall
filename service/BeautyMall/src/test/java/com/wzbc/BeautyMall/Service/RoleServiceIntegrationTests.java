package com.wzbc.BeautyMall.Service;

import com.wzbc.BeautyMall.Mapper.RoleMapper;
import com.wzbc.BeautyMall.Service.impl.RoleServiceImpl;
import com.wzbc.BeautyMall.Service.impl.UserServiceImpl;
import com.wzbc.BeautyMall.entity.DTO.RoleDTO.RoleCreateDTO;
import com.wzbc.BeautyMall.entity.DTO.UserDTO.UserLoginAndRegDTO;
import com.wzbc.BeautyMall.Exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

@org.springframework.test.context.TestExecutionListeners(
        listeners = org.springframework.test.context.support.DependencyInjectionTestExecutionListener.class,
        mergeMode = org.springframework.test.context.TestExecutionListeners.MergeMode.REPLACE_DEFAULTS)
@SpringJUnitConfig(RoleServiceIntegrationTests.Config.class)
class RoleServiceIntegrationTests {
    @Configuration
    @EnableTransactionManagement
    @MapperScan("com.wzbc.BeautyMall.Mapper")
    @Import({RoleServiceImpl.class, UserServiceImpl.class})
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:rbac;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean SqlSessionFactoryBean sqlSessionFactory(DataSource ds) throws Exception {
            SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
            factory.setDataSource(ds);
            factory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
            return factory;
        }
        @Bean DataSourceTransactionManager transactionManager(DataSource ds) {
            return new DataSourceTransactionManager(ds);
        }
        @Bean JdbcTemplate jdbcTemplate(DataSource ds) { return new JdbcTemplate(ds); }
    }
    @Autowired RoleService roleService;
    @Autowired UserService userService;
    @Autowired RoleMapper roleMapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach void schema() {
        jdbc.execute("DROP ALL OBJECTS");
        jdbc.execute("CREATE TABLE sys_user (id BIGINT AUTO_INCREMENT PRIMARY KEY, password VARCHAR(255) NOT NULL, real_name VARCHAR(64) NOT NULL, phone VARCHAR(20), email VARCHAR(254) NOT NULL UNIQUE, avatar VARCHAR(512), status INT NOT NULL, created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL)");
        jdbc.execute("CREATE TABLE sys_role (id BIGINT AUTO_INCREMENT PRIMARY KEY, role_name VARCHAR(50) NOT NULL, role_code VARCHAR(50) NOT NULL UNIQUE, status INT NOT NULL, sort_order INT NOT NULL, remark VARCHAR(500), created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL)");
        jdbc.execute("CREATE TABLE sys_user_role (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT NOT NULL REFERENCES sys_user(id), role_id BIGINT NOT NULL REFERENCES sys_role(id), created_at TIMESTAMP NOT NULL, UNIQUE(user_id, role_id))");
    }
    private Long createRole(String code) {
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode(code);
        dto.setRoleName(code);
        return roleService.createRole(dto);
    }
    private UserLoginAndRegDTO registration() {
        UserLoginAndRegDTO dto = new UserLoginAndRegDTO();
        dto.setRealName("测试用户");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        return dto;
    }
    private Long registeredId() {
        return jdbc.queryForObject("SELECT id FROM sys_user", Long.class);
    }
    @Test void registrationBindsUserByCodeAndReturnsGeneratedIds() {
        createRole("ADMIN");
        Long roleId = createRole("USER");
        userService.RegUser(registration());
        assertEquals(roleId, jdbc.queryForObject("SELECT role_id FROM sys_user_role", Long.class));
        assertEquals(registeredId(), jdbc.queryForObject("SELECT user_id FROM sys_user_role", Long.class));
        assertEquals(java.util.List.of("USER"), roleMapper.selectEnabledCodesByUserId(registeredId()));
    }
    @Test void missingDefaultRoleRollsBackUserInsert() {
        assertThrows(BusinessException.class, () -> userService.RegUser(registration()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
    }
    @Test void disabledDefaultRoleRollsBackUserInsert() {
        createRole("USER");
        jdbc.update("UPDATE sys_role SET status=0");
        assertThrows(BusinessException.class, () -> userService.RegUser(registration()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
    }
    @Test void bindingFailureRollsBackUserInsert() {
        createRole("USER");
        jdbc.execute("ALTER TABLE sys_user_role ADD CONSTRAINT reject_binding CHECK (role_id < 0)");
        assertThrows(RuntimeException.class, () -> userService.RegUser(registration()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
    }
    @Test void repeatedBindingDoesNotDuplicateAndDisabledRoleIsNotAuthorized() {
        Long roleId = createRole("USER");
        userService.RegUser(registration());
        roleService.bindRoleToUser(registeredId(), roleId);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sys_user_role", Integer.class));
        Long admin = createRole("ADMIN");
        roleService.bindRoleToUser(registeredId(), admin);
        assertTrue(roleMapper.selectEnabledCodesByUserId(registeredId()).contains("ADMIN"));
        jdbc.update("UPDATE sys_role SET status=0 WHERE id=?", admin);
        assertFalse(roleMapper.selectEnabledCodesByUserId(registeredId()).contains("ADMIN"));
    }
    @Test void duplicateRoleAndInvalidBindingRejected() {
        Long role = createRole("USER");
        assertThrows(BusinessException.class, () -> createRole("USER"));
        assertThrows(BusinessException.class, () -> roleService.bindRoleToUser(999L, role));
        userService.RegUser(registration());
        assertThrows(BusinessException.class, () -> roleService.bindRoleToUser(registeredId(), 999L));
    }
}
