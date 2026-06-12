package io.github.vivitoto.vanga.ui.home.edit.view

import io.github.vivitoto.vanga.ui.home.BooleanOpState
import io.github.vivitoto.vanga.ui.home.DateOpState
import io.github.vivitoto.vanga.ui.home.EqualityNullableOpState
import io.github.vivitoto.vanga.ui.home.EqualityOpState
import io.github.vivitoto.vanga.ui.home.NumericNullableOpState
import io.github.vivitoto.vanga.ui.home.NumericOpState
import io.github.vivitoto.vanga.ui.home.StringOpState
import io.github.vivitoto.vanga.ui.home.edit.BookFilterEditState
import io.github.vivitoto.vanga.ui.home.edit.BookMatchConditionState.BookConditionType
import io.github.vivitoto.vanga.ui.home.edit.BookSort
import io.github.vivitoto.vanga.ui.home.edit.FilterEditViewModel
import io.github.vivitoto.vanga.ui.home.edit.MatchType
import io.github.vivitoto.vanga.ui.home.edit.SeriesFilterEditState
import io.github.vivitoto.vanga.ui.home.edit.SeriesMatchConditionState.SeriesConditionType
import io.github.vivitoto.vanga.ui.home.edit.SeriesSort
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.book.MediaProfile
import snd.komga.client.common.KomgaSort
import snd.komga.client.search.KomgaSearchCondition.PosterMatch
import snd.komga.client.series.KomgaSeriesStatus

internal fun FilterEditViewModel.FilterType.label(): String = when (this) {
    FilterEditViewModel.FilterType.Series -> "系列"
    FilterEditViewModel.FilterType.Book -> "书籍"
}

internal fun BookFilterEditState.FilterType.label(): String = when (this) {
    BookFilterEditState.FilterType.Custom -> "自定义"
    BookFilterEditState.FilterType.OnDeck -> "继续阅读"
}

internal fun SeriesFilterEditState.FilterType.label(): String = when (this) {
    SeriesFilterEditState.FilterType.Custom -> "自定义"
    SeriesFilterEditState.FilterType.RecentlyAdded -> "最近添加"
    SeriesFilterEditState.FilterType.RecentlyUpdated -> "最近更新"
}

internal fun BookSort.label(): String = when (this) {
    BookSort.Title -> "标题"
    BookSort.CreatedDate -> "创建时间"
    BookSort.SeriesTitle -> "系列标题"
    BookSort.PagesCount -> "页数"
    BookSort.ReleaseDate -> "发行日期"
    BookSort.LastModified -> "最后修改"
    BookSort.Number -> "序号"
    BookSort.ReadDate -> "阅读时间"
    BookSort.Unsorted -> "不排序"
}

internal fun SeriesSort.label(): String = when (this) {
    SeriesSort.Title -> "标题"
    SeriesSort.CreatedDate -> "创建时间"
    SeriesSort.LastModifiedDate -> "最后修改"
    SeriesSort.ReleaseDate -> "发行日期"
    SeriesSort.BookCount -> "书籍数量"
    SeriesSort.Unsorted -> "不排序"
}

internal fun KomgaSort.Direction.label(): String = when (name) {
    "ASC" -> "升序"
    "DESC" -> "降序"
    else -> name
}

internal fun MatchType.label(): String = when (this) {
    MatchType.Any -> "任一条件"
    MatchType.All -> "全部条件"
}

internal fun BookConditionType.label(): String = when (this) {
    BookConditionType.AnyOf -> "任一条件组"
    BookConditionType.AllOf -> "全部条件组"
    BookConditionType.Author -> "作者"
    BookConditionType.Deleted -> "已删除"
    BookConditionType.Library -> "书库"
    BookConditionType.MediaProfile -> "媒体类型"
    BookConditionType.MediaStatus -> "媒体状态"
    BookConditionType.NumberSort -> "序号"
    BookConditionType.Oneshot -> "单本"
    BookConditionType.Poster -> "封面"
    BookConditionType.ReadList -> "阅读清单"
    BookConditionType.ReadStatus -> "阅读状态"
    BookConditionType.ReleaseDate -> "发行日期"
    BookConditionType.Series -> "系列"
    BookConditionType.Tag -> "标签"
    BookConditionType.Title -> "标题"
}

internal fun SeriesConditionType.label(): String = when (this) {
    SeriesConditionType.AnyOf -> "任一条件组"
    SeriesConditionType.AllOf -> "全部条件组"
    SeriesConditionType.AgeRating -> "年龄分级"
    SeriesConditionType.Author -> "作者"
    SeriesConditionType.Collection -> "合集"
    SeriesConditionType.Complete -> "已完结"
    SeriesConditionType.Deleted -> "已删除"
    SeriesConditionType.Genre -> "类型"
    SeriesConditionType.Language -> "语言"
    SeriesConditionType.Library -> "书库"
    SeriesConditionType.Oneshot -> "单本"
    SeriesConditionType.Publisher -> "出版社"
    SeriesConditionType.ReadStatus -> "阅读状态"
    SeriesConditionType.ReleaseDate -> "发行日期"
    SeriesConditionType.SharingLabel -> "共享标签"
    SeriesConditionType.Status -> "系列状态"
    SeriesConditionType.Tag -> "标签"
    SeriesConditionType.Title -> "标题"
    SeriesConditionType.TitleSort -> "排序标题"
}

internal fun EqualityOpState.Op.label(): String = when (this) {
    EqualityOpState.Op.Equals -> "等于"
    EqualityOpState.Op.NotEquals -> "不等于"
}

internal fun EqualityNullableOpState.Op.label(): String = when (this) {
    EqualityNullableOpState.Op.Equals -> "等于"
    EqualityNullableOpState.Op.NotEquals -> "不等于"
    EqualityNullableOpState.Op.IsNull -> "为空"
    EqualityNullableOpState.Op.IsNotNull -> "不为空"
}

internal fun BooleanOpState.Op.label(): String = when (this) {
    BooleanOpState.Op.True -> "是"
    BooleanOpState.Op.False -> "否"
}

internal fun StringOpState.Op.label(): String = when (this) {
    StringOpState.Op.Equals -> "等于"
    StringOpState.Op.NotEquals -> "不等于"
    StringOpState.Op.Contains -> "包含"
    StringOpState.Op.DoesNotContain -> "不包含"
    StringOpState.Op.BeginsWith -> "开头是"
    StringOpState.Op.DoesNotBeginWith -> "开头不是"
    StringOpState.Op.EndsWith -> "结尾是"
    StringOpState.Op.DoesNotEndWith -> "结尾不是"
}

internal fun DateOpState.Op.label(): String = when (this) {
    DateOpState.Op.IsBefore -> "早于"
    DateOpState.Op.IsAfter -> "晚于"
    DateOpState.Op.IsInLast -> "最近"
    DateOpState.Op.IsNotInLast -> "不在最近"
    DateOpState.Op.IsNull -> "为空"
    DateOpState.Op.IsNotNull -> "不为空"
}

internal fun NumericOpState.Op.label(): String = when (this) {
    NumericOpState.Op.EqualTo -> "等于"
    NumericOpState.Op.NotEqualTo -> "不等于"
    NumericOpState.Op.GreaterThan -> "大于"
    NumericOpState.Op.LessThan -> "小于"
}

internal fun NumericNullableOpState.Op.label(): String = when (this) {
    NumericNullableOpState.Op.EqualTo -> "等于"
    NumericNullableOpState.Op.NotEqualTo -> "不等于"
    NumericNullableOpState.Op.GreaterThan -> "大于"
    NumericNullableOpState.Op.LessThan -> "小于"
    NumericNullableOpState.Op.IsNull -> "为空"
    NumericNullableOpState.Op.IsNotNull -> "不为空"
}

internal fun KomgaReadStatus.label(): String = when (name) {
    "UNREAD" -> "未读"
    "IN_PROGRESS" -> "阅读中"
    "READ" -> "已读"
    else -> name
}

internal fun MediaProfile.label(): String = when (name) {
    "DIVINA" -> "Divina"
    "EPUB" -> "EPUB"
    "PDF" -> "PDF"
    else -> name
}

internal fun KomgaMediaStatus.label(): String = when (name) {
    "READY" -> "可用"
    "UNKNOWN" -> "未知"
    "ERROR" -> "错误"
    "UNSUPPORTED" -> "不支持"
    "OUTDATED" -> "已过期"
    else -> name
}

internal fun KomgaSeriesStatus.label(): String = when (name) {
    "ONGOING" -> "连载中"
    "ENDED" -> "已完结"
    "ABANDONED" -> "已放弃"
    "HIATUS" -> "暂停"
    else -> name
}

internal fun PosterMatch.Type?.label(): String = when (this?.name) {
    null -> "任意"
    "SIDECAR" -> "旁载文件"
    "USER_UPLOADED" -> "用户上传"
    else -> this?.name ?: "任意"
}

internal fun Boolean?.anyLabel(): String = when (this) {
    null -> "任意"
    true -> "是"
    false -> "否"
}
