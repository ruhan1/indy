package org.commonjava.aprox.httprox.conf;

import java.io.File;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.commonjava.aprox.conf.AbstractAproxConfigInfo;
import org.commonjava.aprox.conf.AbstractAproxFeatureConfig;
import org.commonjava.aprox.conf.AproxConfigClassInfo;
import org.commonjava.aprox.conf.AproxConfigInfo;
import org.commonjava.web.config.ConfigurationException;
import org.commonjava.web.config.annotation.ConfigName;
import org.commonjava.web.config.annotation.SectionName;

@SectionName( "httprox" )
@Alternative
@Named
public class HttproxConfig
{

    private static final int DEFAULT_PORT = 8081;

    private static final boolean DEFAULT_ENABLED = false;

    private static final String DEFAULT_PROXY_REALM = "httprox";

    private static final boolean DEFAULT_SECURED = true;

    private static final String DEFAULT_TRACKING_TYPE = TrackingType.SUFFIX.name();

    private String proxyRealm;

    private Boolean enabled;

    private Boolean secured;

    private Integer port;

    private String trackingType;

    public TrackingType getTrackingType()
    {
        return TrackingType.valueOf( trackingType == null ? DEFAULT_TRACKING_TYPE : trackingType.toUpperCase() );
    }

    @ConfigName( "tracking.type" )
    public void setTrackingType( final String option )
    {
        this.trackingType = option;
    }

    public void setTrackingType( final TrackingType type )
    {
        this.trackingType = type.name();
    }

    public boolean isEnabled()
    {
        return enabled == null ? DEFAULT_ENABLED : enabled;
    }

    @ConfigName( "enabled" )
    public void setEnabled( final Boolean enabled )
    {
        this.enabled = enabled;
    }

    public boolean isSecured()
    {
        return secured == null ? DEFAULT_SECURED : secured;
    }

    @ConfigName( "secured" )
    public void setSecured( final Boolean secured )
    {
        this.secured = secured;
    }

    public Integer getPort()
    {
        return port == null ? DEFAULT_PORT : port;
    }

    @ConfigName( "port" )
    public void setPort( final Integer port )
    {
        this.port = port;
    }

    public String getProxyRealm()
    {
        return proxyRealm == null ? DEFAULT_PROXY_REALM : proxyRealm;
    }

    @ConfigName( "proxy.realm" )
    public void setProxyRealm( final String proxyRealm )
    {
        this.proxyRealm = proxyRealm;
    }

    @javax.enterprise.context.ApplicationScoped
    public static class ConfigInfo
        extends AbstractAproxConfigInfo
    {
        public ConfigInfo()
        {
            super( HttproxConfig.class );
        }

        @Override
        public String getDefaultConfigFileName()
        {
            return new File( AproxConfigInfo.CONF_INCLUDES_DIR, "httprox.conf" ).getPath();
        }

        @Override
        public InputStream getDefaultConfig()
        {
            return Thread.currentThread()
                         .getContextClassLoader()
                         .getResourceAsStream( "default-httprox.conf" );
        }
    }

    @javax.enterprise.context.ApplicationScoped
    public static class FeatureConfig
        extends AbstractAproxFeatureConfig<HttproxConfig, HttproxConfig>
    {
        @Inject
        private ConfigInfo info;

        public FeatureConfig()
        {
            super( HttproxConfig.class );
        }

        @Produces
        @Default
        @ApplicationScoped
        public HttproxConfig getFlatFileConfig()
            throws ConfigurationException
        {
            return getConfig();
        }

        @Override
        public AproxConfigClassInfo getInfo()
        {
            return info;
        }
    }

}
