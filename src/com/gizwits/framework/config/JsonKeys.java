/**
 * Project Name:XPGSdkV4AppBase
 * File Name:JsonKeys.java
 * Package Name:com.gizwits.framework.config
 * Date:2015-1-27 14:47:10
 * Copyright (c) 2014~2015 Xtreme Programming Group, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gizwits.framework.config;

// TODO: Auto-generated Javadoc
/**
 * 
 * ClassName: Class JsonKeys. <br/>
 * Json对应字段表<br/>
 * 
 * @author Lien
 */
public class JsonKeys {

	/** 产品名. */
	public final static String PRODUCT_NAME = "机智云智能热水器";

	/** 实体字段名，代表对应的项目. */
	public final static String KEY_ACTION = "entity0";

	/** 开关. */
	public final static String ON_OFF = "Switch";

	/** 目标温度 30 - 75. */
	public final static String SET_TEMP = "Set_Temp";
	
	/** 当前温度 0 - 99. */
	public final static String ROOM_TEMP = "Room_Temp";
	
	/** 模式切换 0、智能模式 1、节能模式 2、速热模式 3、加热模式 4、保温模式 5、安全模式. */
	public final static String MODE = "Mode";
	
	
	/** 倒计时预约. */
	public final static String COUNT_DOWN_RESERVE = "CountDown_Reserve";
	
	/** 定时预约. */
	public final static String TIME_RESERVE = "Time_Reserve";
	
	/** 预约开关. */
	public final static String RESERVE_ON_OFF = "Reserve_OnOff";
	
	/** 时钟校准. */
	public final static String CALIBRATION_TIME = "Calibration_Time";
	
	

	/** 干烧故障. */
	public final static String FAULT_BURNING = "Fault_burning";

	/** 传感器开路故障. */
	public final static String FAULT_SENSOR_OPEN = "Fault_SensorOpen";

	/** 传感器短路故障. */
	public final static String FAULT_SENSOR_SHORT = "Fault_SensorShort";
	
	/** 超温故障. */
	public final static String FAULT_OVER_TEMP = "Fault_OverTemp";
}
