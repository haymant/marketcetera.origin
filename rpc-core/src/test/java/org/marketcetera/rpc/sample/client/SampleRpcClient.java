package org.marketcetera.rpc.sample.client;

import org.marketcetera.core.Util;
import org.marketcetera.core.Version;
import org.marketcetera.core.VersionInfo;
import org.marketcetera.rpc.base.BaseRpc.HeartbeatRequest;
import org.marketcetera.rpc.base.BaseRpc.HeartbeatResponse;
import org.marketcetera.rpc.base.BaseRpc.LoginRequest;
import org.marketcetera.rpc.base.BaseRpc.LoginResponse;
import org.marketcetera.rpc.base.BaseRpc.LogoutRequest;
import org.marketcetera.rpc.base.BaseRpc.LogoutResponse;
import org.marketcetera.rpc.client.AbstractRpcClient;
import org.marketcetera.rpc.sample.SampleRpcServiceGrpc;
import org.marketcetera.rpc.sample.SampleRpcServiceGrpc.SampleRpcServiceBlockingStub;
import org.marketcetera.rpc.sample.SampleRpcServiceGrpc.SampleRpcServiceStub;
import org.marketcetera.util.ws.tags.AppId;

import io.grpc.Channel;
import io.grpc.stub.StreamObserver;

/* $License$ */

/**
 *
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
public class SampleRpcClient
        extends AbstractRpcClient<SampleRpcServiceBlockingStub,SampleRpcServiceStub>
{
    /* (non-Javadoc)
     * @see org.marketcetera.rpc.client.AbstractRpcClient#getBlockingStub(io.grpc.Channel)
     */
    @Override
    protected SampleRpcServiceBlockingStub getBlockingStub(Channel inChannel)
    {
        return SampleRpcServiceGrpc.newBlockingStub(inChannel);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.rpc.client.AbstractRpcClient#getAsyncStub(io.grpc.Channel)
     */
    @Override
    protected SampleRpcServiceStub getAsyncStub(Channel inChannel)
    {
        return SampleRpcServiceGrpc.newStub(inChannel);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.rpc.client.AbstractRpcClient#executeLogin(org.marketcetera.rpc.base.BaseRpc.LoginRequest)
     */
    @Override
    protected LoginResponse executeLogin(LoginRequest inRequest)
    {
        return getBlockingStub().login(inRequest);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.rpc.client.AbstractRpcClient#executeLogout(org.marketcetera.rpc.base.BaseRpc.LogoutRequest)
     */
    @Override
    protected LogoutResponse executeLogout(LogoutRequest inRequest)
    {
        return getBlockingStub().logout(inRequest);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.rpc.client.AbstractRpcClient#executeHeartbeat(org.marketcetera.rpc.base.BaseRpc.HeartbeatRequest, io.grpc.stub.StreamObserver)
     */
    @Override
    protected void executeHeartbeat(HeartbeatRequest inRequest,
                                    StreamObserver<HeartbeatResponse> inObserver)
    {
        getAsyncStub().heartbeat(inRequest,
                                 inObserver);
    }
    /* (non-Javadoc)
     * @see org.marketcetera.rpc.client.AbstractRpcClient#getAppId()
     */
    @Override
    protected AppId getAppId()
    {
        return APP_ID;
    }
    /* (non-Javadoc)
     * @see org.marketcetera.rpc.client.AbstractRpcClient#getVersionInfo()
     */
    @Override
    protected VersionInfo getVersionInfo()
    {
        return APP_ID_VERSION;
    }
    /**
     * The client's application ID: the application name.
     */
    private static final String APP_ID_NAME = SampleRpcClient.class.getSimpleName();
    /**
     * The client's application ID: the version.
     */
    private static final VersionInfo APP_ID_VERSION = new VersionInfo(Version.pomversion);
    /**
     * The client's application ID: the ID.
     */
    private static final AppId APP_ID = Util.getAppId(APP_ID_NAME,APP_ID_VERSION.getVersionInfo());
}
