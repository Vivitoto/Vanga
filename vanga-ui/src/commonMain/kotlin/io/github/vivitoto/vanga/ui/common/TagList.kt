package io.github.vivitoto.vanga.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.common.components.DescriptionChips
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry.Companion.stringEntry

@Composable
fun TagList(
    tags: List<String>,
    secondaryTags: List<String>? = null,
    onTagClick: (String) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val mutableTagList = remember(tags) { tags.toMutableList() }
        val mutableSecondaryTagList = remember(secondaryTags) { secondaryTags?.toMutableList() }

        val parodyTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "原作：")
        }
        val secondaryParodyTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let {
                extractTagListByPrefix(it, "原作：")
            }
        }

        val characterTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "角色：")
        }
        val secondaryCharacterTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let { extractTagListByPrefix(it, "角色：") }
        }

        val groupTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "社团：")
        }
        val secondaryGroupTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let { extractTagListByPrefix(it, "社团：") }
        }

        val femaleTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "女性：")
        }
        val secondaryFemaleTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let {
                extractTagListByPrefix(it, "女性：")
            }
        }
        val maleTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "男性：")
        }
        val secondaryMaleTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let {
                extractTagListByPrefix(it, "男性：")
            }
        }
        val categoryTags = remember(tags) {
            extractTagListByPrefix(mutableTagList, "分类：")
        }
        val secondaryCategoryTags = remember(secondaryTags) {
            mutableSecondaryTagList?.let {
                extractTagListByPrefix(it, "分类：")
            }
        }

        val tagEntries = remember(tags) {
            mutableTagList.map { stringEntry(it) }
        }
        val secondaryTagEntries = remember(secondaryTags) {
            mutableSecondaryTagList?.map { stringEntry(it) }
        }

        if (tagEntries.size == tags.size && secondaryTags?.size == secondaryTagEntries?.size) {
            DescriptionChips(
                label = "标签",
                chipValues = tagEntries,
                secondaryValues = secondaryTagEntries,
                onChipClick = onTagClick,
            )
        } else {
            if (parodyTags.isNotEmpty() || !secondaryParodyTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "原作",
                    chipValues = parodyTags,
                    secondaryValues = secondaryParodyTags,
                    onChipClick = onTagClick,
                )
            }

            if (characterTags.isNotEmpty() || !secondaryCharacterTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "角色",
                    chipValues = characterTags,
                    secondaryValues = secondaryCharacterTags,
                    onChipClick = onTagClick,
                )
            }

            if (groupTags.isNotEmpty() || !secondaryGroupTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "社团",
                    chipValues = groupTags,
                    secondaryValues = secondaryGroupTags,
                    onChipClick = onTagClick,
                )
            }

            if (femaleTags.isNotEmpty() || !secondaryFemaleTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "女性向标签",
                    chipValues = femaleTags,
                    secondaryValues = secondaryFemaleTags,
                    onChipClick = onTagClick,
                )
            }
            if (maleTags.isNotEmpty() || !secondaryMaleTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "男性向标签",
                    chipValues = maleTags,
                    secondaryValues = secondaryMaleTags,
                    onChipClick = onTagClick,
                )
            }
            if (categoryTags.isNotEmpty() || !secondaryCategoryTags.isNullOrEmpty()) {
                DescriptionChips(
                    label = "分类",
                    chipValues = categoryTags,
                    secondaryValues = secondaryCategoryTags,
                    onChipClick = onTagClick,
                )
            }
            DescriptionChips(
                label = "其他标签",
                chipValues = tagEntries,
                secondaryValues = secondaryTagEntries,
                onChipClick = onTagClick,
            )

        }

    }
}

private fun extractTagListByPrefix(
    tags: MutableList<String>,
    prefix: String
): List<LabeledEntry<String>> {
    val results = mutableListOf<LabeledEntry<String>>()
    val iterator = tags.iterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (element.startsWith(prefix)) {
            results.add(LabeledEntry(element, element.removePrefix(prefix)))
            iterator.remove()
        }
    }
    return results
}

//    tags.filter { it.startsWith(prefix) }
//        .map { LabeledEntry(it, it.removePrefix(prefix)) }
