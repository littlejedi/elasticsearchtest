package elasticsearchclient;

import com.liangzhi.commons.mybatis.HasValueEnum;

public enum SensorDataType implements HasValueEnum {
	NUMBER(1),
	IMAGE(2),
	VIDEO(3);

	private int value;
	SensorDataType(int value) {
		this.value = value;
	}
	
	public int getValue() {
	    return this.value;
    }
}
