package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new kr.co.bbmc.paycast.DataBinderMapperImpl());
  }
}
