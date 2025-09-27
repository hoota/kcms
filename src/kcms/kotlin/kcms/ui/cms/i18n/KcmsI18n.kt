package kcms.ui.cms.i18n

import kcms.common.nullIfBlank
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

interface KcmsI18n {

    val areYouSure: String
    val remove: String
    val chooseFile: String
    val fileUrl: String
    val upload: String
    val search: String
    val anyParent: String
    val anyTemplate: String
    val searchInContent: String
    val searchQuery: String
    val noParent: String
    val pageSlug: String
    val parent: String
    val removePage: String
    val saveAndContinue: String
    val newPage: String
    val no: String
    val yes: String
    val published: String
    val template: String
    val title: String
    val children: String
    val properties: String
    val page: String
    val runJob: String
    val status: String
    val jobName: String
    val sharedFiles: String
    val enumCategories: String
    val addCategory: String
    val category: String
    val enums: String
    val backgroundJobs: String
    val files: String
    val settings: String
    val pages: String
    val totalFound: String
    val moveToTheBottom: String
    val moveDown: String
    val moveUp: String
    val moveToTop: String
    val edit: String
    val save: String
    val close: String
    val value: String
    val enumCategory: String
    val newValues: String
    val more: String
}

@Component
class KcmsInternationalization(
    val kcmsI18nEn: KcmsI18nEn,
    val kcmsI18nRu: KcmsI18nRu,
) {
    @Value("\${cms.lang}")
    lateinit var lang: String

    @PostConstruct
    fun postConstruct() {
        instance = if(lang.equals("ru", ignoreCase = true)) kcmsI18nRu else kcmsI18nEn
        language = lang.nullIfBlank()?.lowercase() ?: "en"
    }

    companion object {
        lateinit var language: String
        lateinit var instance: KcmsI18n
    }
}