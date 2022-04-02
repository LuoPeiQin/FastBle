/**
 * Copyright (C), 2007-2022, 未来穿戴有限公司
 * FileName: ExDeviceEnum
 * Author: lpq
 * Date: 2022/4/2 17:53
 * Description: 用一句话描述下
 */
package com.luo.skg.exdevice.config

/**
 *
 * @ProjectName: FastBle
 * @Package: com.luo.skg.exdevice.config
 * @ClassName: ExDeviceEnum
 * @Description: 用一句话描述下
 * @Author: lpq
 * @CreateDate: 2022/4/2 17:53
 */
enum class ExDeviceEnum(val bleName: String,val skgType: String,val description: String) {

    SKG_WEI_CE_BLOOD_SUGAR("SKG_VGM55", "倍科3805","微策血糖仪"),

    SKG_AO_JI_BLOOD_PRESSURE("SKG_AOJ-30B", "倍科8703","奥极血压计"),

}