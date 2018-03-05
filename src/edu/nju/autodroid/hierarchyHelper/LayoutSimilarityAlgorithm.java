package edu.nju.autodroid.hierarchyHelper;

/**
 * Created by ysht on 2017/11/12.
 */
public enum LayoutSimilarityAlgorithm {
    //广度优先展开成向量，计算向量之间的相似度
    BFSThenEditdistane,

    //匹配矩形区域的重叠
    RectArea,

    //匹配面积占比
    RegionRatio,
}
