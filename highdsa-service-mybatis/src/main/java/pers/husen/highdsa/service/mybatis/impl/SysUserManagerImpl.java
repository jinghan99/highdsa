package pers.husen.highdsa.service.mybatis.impl;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pers.husen.highdsa.common.entity.po.shiro.SysUser;
import pers.husen.highdsa.common.entity.po.shiro.SysUserRole;
import pers.husen.highdsa.service.mybatis.SysUserManager;
import pers.husen.highdsa.service.mybatis.dao.system.SysUserMapper;
import pers.husen.highdsa.service.mybatis.dao.system.SysUserRoleMapper;

/**
 * @Desc 系统用户管理实现
 *
 * @Author 何明胜
 *
 * @Created at 2018年3月29日 上午9:29:44
 * 
 * @Version 1.0.4
 */
@Service("sysUserManager")
public class SysUserManagerImpl implements SysUserManager {

	@Autowired
	private SysUserMapper sysUserMapper;
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;

	/**
	 * 创建用户
	 * 
	 * @param user
	 */
	@Override
	public int createUser(SysUser user) {
		// 加密密码
		encryptPassword(user);
		return sysUserMapper.insert(user);
	}

	/**
	 * 修改密码
	 * 
	 * @param userId
	 * @param newPassword
	 */
	@Override
	public void modifyPassword(Long userId, String newPassword) {
		SysUser user = sysUserMapper.selectByPrimaryKey(userId);
		user.setUserPassword(newPassword);
		encryptPassword(user);
		sysUserMapper.updateByPrimaryKey(user);
	}

	/**
	 * 添加用户-角色关系
	 * 
	 * @param userId
	 * @param roleIds
	 */
	@Override
	public void correlationRoles(Long userId, Long... roleIds) {
		for (Long roleId : roleIds) {
			sysUserRoleMapper.insert(new SysUserRole(userId, roleId));
		}
	}

	/**
	 * 移除用户-角色关系
	 * 
	 * @param userId
	 * @param roleIds
	 */
	@Override
	public void uncorrelationRoles(Long userId, Long... roleIds) {
		for (Long roleId : roleIds) {
			sysUserRoleMapper.deleteByPrimaryKey(userId, roleId);
		}
	}

	/**
	 * 根据用户名查找用户
	 * 
	 * @param userName
	 * @return
	 */
	@Override
	public SysUser findByUserName(String userName) {
		return sysUserMapper.selectUserInfoByUserName(userName);
	}

	/**
	 * 根据用户名查找其角色
	 * 
	 * @param userName
	 * @return
	 */
	@Override
	public SysUser findRoles(String userName) {
		return sysUserMapper.selectRolesByUserName(userName);
	}

	/**
	 * 根据用户名查找其权限
	 * 
	 * @param userName
	 * @return
	 */
	@Override
	public SysUser findPermissions(String userName) {
		return sysUserMapper.selectPermissionsByUserName(userName);
	}

	/**
	 * 密码加密
	 * 
	 * @param user
	 */
	public void encryptPassword(SysUser user) {
		RandomNumberGenerator randomNumberGenerator = new SecureRandomNumberGenerator();
		String algorithmName = "md5";
		final int hashIterations = 2;

		user.setUserPwdSalt(randomNumberGenerator.nextBytes().toHex());
		String newPassword = new SimpleHash(algorithmName, user.getUserPassword(), ByteSource.Util.bytes(user.getUserName() + user.getUserPwdSalt()), hashIterations).toHex();

		user.setUserPassword(newPassword);
	}
}