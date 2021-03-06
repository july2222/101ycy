package com.ruoyi.framework.util;

import com.ruoyi.common.config.Global;
import com.ruoyi.common.json.JSON;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.framework.shiro.realm.UserRealm;
import com.ruoyi.system.domain.SysUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro 工具类
 * 
 * @author ruoyi
 */
public class ShiroUtils
{
    public static Subject getSubject()
    {
        return SecurityUtils.getSubject();
    }

    public static Session getSession()
    {
        return SecurityUtils.getSubject().getSession();
    }

    public static void logout()
    {
        getSubject().logout();
    }

    /**
     * shiro 的用户信息
     * @return
     */
    public static SysUser getSysUser()
    {
        SysUser user = null;
        Object obj = getSubject().getPrincipal();
        if (StringUtils.isNotNull(obj))
        {
            user = new SysUser();
            BeanUtils.copyBeanProp(user, obj);
        }
        return user;
    }

    /**
     * JWT 的用户信息
     * @return
     */
    public static SysUser getSysUserByJwtToken(String token)
    {
        SysUser user = null;
        Object obj = EHCacheUtil.getValue("jwt",token);
        if (StringUtils.isNotNull(obj))
        {
            user = new SysUser();
            BeanUtils.copyBeanProp(user, obj);
        }
        return user;
    }

    public static void setSysUser(SysUser user)
    {
        Subject subject = getSubject();
        PrincipalCollection principalCollection = subject.getPrincipals();
        String realmName = principalCollection.getRealmNames().iterator().next();
        PrincipalCollection newPrincipalCollection = new SimplePrincipalCollection(user, realmName);
        // 重新加载Principal
        subject.runAs(newPrincipalCollection);
    }

    public static void clearCachedAuthorizationInfo()
    {
        RealmSecurityManager rsm = (RealmSecurityManager) SecurityUtils.getSecurityManager();
        UserRealm realm = (UserRealm) rsm.getRealms().iterator().next();
        realm.clearCachedAuthorizationInfo();
    }

    public static String getUserId()
    {
        return getSysUser().getUserId();
    }

    public static String getLoginName()
    {
        return getSysUser().getLoginName();
    }

    public static String getIp()
    {
        return getSubject().getSession().getHost();
    }

    public static String getSessionId()
    {
        return String.valueOf(getSubject().getSession().getId());
    }

    /**
     * 生成随机盐
     */
    public static String randomSalt()
    {
        // 一个Byte占两个字节，此处生成的3字节，字符串长度为6
        SecureRandomNumberGenerator secureRandom = new SecureRandomNumberGenerator();
        String hex = secureRandom.nextBytes(3).toHex();
        return hex;
    }

    /**
     * jwt登录成功后返回给前端的用户信息
     */
    public static Map<String,Object> getJwtUserInfo() {
        SysUser sysUser = getSysUser();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("loginName",sysUser.getLoginName());
        map.put("realName",sysUser.getUserName());
        map.put("sex",sysUser.getSex());
        map.put("sexStr",sysUser.getSexStr());
        map.put("phonenumber",sysUser.getPhonenumber());
        map.put("avatarUrl",Global.getAvatarPath()+sysUser.getAvatar());
        map.put("email",sysUser.getEmail());
        return map;
    }

    /**
     * jwt登录成功后返回给前端的用户信息token
     * @return
     */
    public static String getJwtUserInfoToken() {
        try {
            SysUser sysUser = getSysUser();
            sysUser.setPassword(null);//安全一点
            return JwtUtils.createToken(JSON.toJSONString(sysUser), Global.getJwtOutTime());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
