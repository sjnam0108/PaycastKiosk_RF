/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package service.vcat.smartro.com.vcat;
public interface SmartroVCatInterface extends android.os.IInterface
{
  /** Default implementation for SmartroVCatInterface. */
  public static class Default implements service.vcat.smartro.com.vcat.SmartroVCatInterface
  {
    @Override public void executeService(java.lang.String strJSON, service.vcat.smartro.com.vcat.SmartroVCatCallback svcbPoint) throws android.os.RemoteException
    {
    }
    @Override public void postExtraData(java.lang.String strJSON) throws android.os.RemoteException
    {
    }
    @Override public void cancelService() throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements service.vcat.smartro.com.vcat.SmartroVCatInterface
  {
    private static final java.lang.String DESCRIPTOR = "service.vcat.smartro.com.vcat.SmartroVCatInterface";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an service.vcat.smartro.com.vcat.SmartroVCatInterface interface,
     * generating a proxy if needed.
     */
    public static service.vcat.smartro.com.vcat.SmartroVCatInterface asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof service.vcat.smartro.com.vcat.SmartroVCatInterface))) {
        return ((service.vcat.smartro.com.vcat.SmartroVCatInterface)iin);
      }
      return new service.vcat.smartro.com.vcat.SmartroVCatInterface.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_executeService:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          service.vcat.smartro.com.vcat.SmartroVCatCallback _arg1;
          _arg1 = service.vcat.smartro.com.vcat.SmartroVCatCallback.Stub.asInterface(data.readStrongBinder());
          this.executeService(_arg0, _arg1);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_postExtraData:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.postExtraData(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_cancelService:
        {
          data.enforceInterface(descriptor);
          this.cancelService();
          reply.writeNoException();
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements service.vcat.smartro.com.vcat.SmartroVCatInterface
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void executeService(java.lang.String strJSON, service.vcat.smartro.com.vcat.SmartroVCatCallback svcbPoint) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(strJSON);
          _data.writeStrongBinder((((svcbPoint!=null))?(svcbPoint.asBinder()):(null)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_executeService, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().executeService(strJSON, svcbPoint);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void postExtraData(java.lang.String strJSON) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(strJSON);
          boolean _status = mRemote.transact(Stub.TRANSACTION_postExtraData, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().postExtraData(strJSON);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void cancelService() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_cancelService, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().cancelService();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static service.vcat.smartro.com.vcat.SmartroVCatInterface sDefaultImpl;
    }
    static final int TRANSACTION_executeService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_postExtraData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_cancelService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    public static boolean setDefaultImpl(service.vcat.smartro.com.vcat.SmartroVCatInterface impl) {
      // Only one user of this interface can use this function
      // at a time. This is a heuristic to detect if two different
      // users in the same process use this function.
      if (Stub.Proxy.sDefaultImpl != null) {
        throw new IllegalStateException("setDefaultImpl() called twice");
      }
      if (impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static service.vcat.smartro.com.vcat.SmartroVCatInterface getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void executeService(java.lang.String strJSON, service.vcat.smartro.com.vcat.SmartroVCatCallback svcbPoint) throws android.os.RemoteException;
  public void postExtraData(java.lang.String strJSON) throws android.os.RemoteException;
  public void cancelService() throws android.os.RemoteException;
}
