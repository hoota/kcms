package kcms.ui.cms.i18n

import org.springframework.stereotype.Component

@Component
class KcmsI18nEn : KcmsI18n {
    override val more: String get() = "More..."
    override val newValue: String
        get() = "New Value"
    override val enumCategory: String
        get() = "Enum Category"
    override val value: String
        get() = "Value"
    override val save: String
        get() = "Save"
    override val moveToTop: String
        get() = "Move on top"
    override val moveUp: String
        get() = "Move up"
    override val moveDown: String
        get() = "Move down"
    override val moveToTheBottom: String
        get() = "Move to the bottom"
    override val pages: String
        get() = "Pages"
    override val templates: String
        get() = "Templates"
    override val files: String
        get() = "Files"
    override val backgroundJobs: String
        get() = "Background Jobs"
    override val enums: String
        get() = "Enums"
    override val category: String
        get() = "Category"
    override val enumCategories: String
        get() = "Enum Categories"
    override val sharedFiles: String
        get() = "Shared Files"
    override val jobName: String
        get() = "Job Name"
    override val status: String
        get() = "Status"
    override val runJob: String
        get() = "Run job"
    override val page: String
        get() = "Page"
    override val properties: String
        get() = "Properties"
    override val children: String
        get() = "Children"
    override val title: String
        get() = "Title"
    override val template: String
        get() = "Template"
    override val published: String
        get() = "Published"
    override val yes: String
        get() = "yes"
    override val no: String
        get() = "no"
    override val newPage: String
        get() = "New Page"
    override val saveAndContinue: String
        get() = "Save and Continue"
    override val removePage: String
        get() = "Remove Page"
    override val parent: String
        get() = "Parent"
    override val pageSlug: String
        get() = "Page Slug (URI)"
    override val noParent: String
        get() = "no parent"
    override val searchQuery: String
        get() = "Search query"
    override val searchInContent: String
        get() = "search in content"
    override val anyTemplate: String
        get() = "any template"
    override val anyParent: String
        get() = "any parent"
    override val search: String
        get() = "Search"
    override val upload: String
        get() = "Upload"
    override val fileUrl: String
        get() = "File URL"
    override val chooseFile: String
        get() = "Choose file"
    override val remove: String
        get() = "remove"
    override val areYouSure: String
        get() = "Are you sure?"
}