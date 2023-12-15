/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package service.vcat.smartro.com.vcat;
/**
  *  This interface class is used to make your application.
  *  Please do not modify below codes if you do not want to get errors on your application.
  *
  *  이 인터페이스 클래스는 어플케이션 제작에 도움을 주기 위해 만들어졌습니다.
  *  오류 발생을 막기 위해, 아래 코드는 수정하지 마시길 바랍니다.
  *
  *  ----------------------------------------------------------------------------------------
  *  최종 수정일 : 2019/01/09
  *  작업자 : JINKYU LEE (jglee@smartro.co.kr)
  *  ----------------------------------------------------------------------------------------
  **/
public interface SmartroVCatCallback extends android.os.IInterface
{
  /** Default implementation for SmartroVCatCallback. */
  public static class Default implements service.vcat.smartro.com.vcat.SmartroVCatCallback
  {
    @Override public void onServiceEvent(java.lang.String strEventJSON) throws android.os.RemoteException
    {
    }
    @Override public void onServiceResult(java.lang.String strResultJSON) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements service.vcat.smartro.com.vcat.SmartroVCatCallback
  {
    private static final java.lang.String DESCRIPTOR = "service.vcat.smartro.com.vcat.SmartroVCatCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an service.vcat.smartro.com.vcat.SmartroVCatCallback interface,
     * generating a proxy if needed.
     */
    public static service.vcat.smartro.com.vcat.SmartroVCatCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof service.vcat.smartro.com.vcat.SmartroVCatCallback))) {
        return ((service.vcat.smartro.com.vcat.SmartroVCatCallback)iin);
      }
      return new service.vcat.smartro.com.vcat.SmartroVCatCallback.Stub.Proxy(obj);
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
        case TRANSACTION_onServiceEvent:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.onServiceEvent(_arg0);
          return true;
        }
        case TRANSACTION_onServiceResult:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.onServiceResult(_arg0);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements service.vcat.smartro.com.vcat.SmartroVCatCallback
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
      @Override public void onServiceEvent(java.lang.String strEventJSON) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(strEventJSON);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onServiceEvent, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onServiceEvent(strEventJSON);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onServiceResult(java.lang.String strResultJSON) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(strResultJSON);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onServiceResult, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onServiceResult(strResultJSON);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static service.vcat.smartro.com.vcat.SmartroVCatCallback sDefaultImpl;
    }
    static final int TRANSACTION_onServiceEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onServiceResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(service.vcat.smartro.com.vcat.SmartroVCatCallback impl) {
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
    public static service.vcat.smartro.com.vcat.SmartroVCatCallback getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void onServiceEvent(java.lang.String strEventJSON) throws android.os.RemoteException;
  public void onServiceResult(java.lang.String strResultJSON) throws android.os.RemoteException;
}
