package kcms.files

import kcms.common.nullIfBlank
import kcms.ui.KcmsGossRenderer
import kiss.gossr.spring.RoutesHelper

class KcmsFilesListBlock(
    val pageId: Long?,
    val files: List<PageFile>
) : KcmsGossRenderer() {
    fun draw(title: KcmsGossRenderer.() -> Unit = {}) {
        SCRIPT(src = "https://cdn.jsdelivr.net/npm/@fancyapps/ui@5.0/dist/fancybox/fancybox.umd.js")
        LINK(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/@fancyapps/ui@5.0/dist/fancybox/fancybox.css")

        drawUploadForm()

        title()

        drawTable()

        SCRIPT(code = """
            $(document).ready(function() {
                Fancybox.bind("[data-fancybox]", {  });
            });                
        """)

    }

    private fun drawTable() {
        FORM(
            KcmsFilesController.KcmsFileRemoveRoute(
                pageId = pageId,
                fileId = 0
            )
        ) { route ->
            id("file-remove-form")
            HIDDEN(route::pageId)
            HIDDEN(route::fileId)
        }

        STYLE("""
            tr:first-of-type span.move-value-up { display: none; }          
            tr:last-of-type span.move-value-down { display: none; }          
        """)

        SCRIPT(code = """
function onRemove(fileId) {
    if(window.confirm(${toJson(i18n.areYouSure)})) {
        const form = $('#file-remove-form')[0];
        form.fileId.value = fileId;
        form.submit();
    }
    return false;
}
        """)

        FORM(KcmsFilesController.KcmsFilesOrderSaveRoute()) { route ->
            ajaxForm()
            TABLE("table") {
                TBODY {
                    files.sortedBy { it.order }.forEach { f ->
                        TR {
                            TD {
                                +f.id.toString()
                                namePrefix(route::orders) {
                                    INPUT("order") {
                                        name(f.id.toString())
                                        type("hidden")
                                        value(f.order)
                                    }
                                }
                                DIV {
                                    SPAN("btn btn-link move-value-up") {
                                        style("border: none; padding: 0 0;")
                                        title("Move on top")
                                        onClick("moveRowOnTop(this, true)")
                                        +"⤒"
                                    }
                                    SPAN("btn btn-link move-value-up") {
                                        style("border: none; padding: 0 0;")
                                        title("Move up")
                                        onClick("moveRowUp(this, true)")
                                        +"↑"
                                    }
                                }
                                DIV {
                                    SPAN("btn btn-link move-value-down") {
                                        style("border: none; padding: 0 0;")
                                        title("Move to the bottom")
                                        onClick("moveRowToBottom(this, true)")
                                        +"⤓"
                                    }
                                    SPAN("btn btn-link move-value-down") {
                                        style("border: none; padding: 0 0;")
                                        title("Move down")
                                        onClick("moveRowDown(this, true)")
                                        +"↓"
                                    }
                                }
                            }
                            TD {
                                if(f.symlink != null) {
                                    classes("font-italic text-secondary")
                                    title("symlink to ${f.symlink}")
                                }
                                +(f.origName.nullIfBlank() ?: f.type.name)
                            }
                            TD {
                                A {
                                    data("fancybox", "photos")
                                    target("_blank")
                                    href(f.url())

                                    if(f.type.image) {
                                        IMG(src = f.urlWithHeight(100), height = 100)
                                    } else {
                                        +f.origName
                                    }
                                }
                            }
                            TD {
                                BUTTON("btn btn-sm btn-danger") {
                                    style("float: right;")
                                    onClick("onRemove(${f.id})")
                                    +i18n.remove
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun drawUploadForm() {
        val route = KcmsFilesController.KcmsFilesUploadRoute(pageId = pageId)

        SCRIPT(code ="""
document.addEventListener('paste', async (event) => {
      const items = event.clipboardData.items;
      for (const item of items) {
        if (item.kind === 'file') {
          const file = item.getAsFile();
          if(file) {
            const formData = new FormData();
            formData.append('${route::files.name}', file);
            formData.append('${route::pageId.name}', ${pageId ?: ""});

            try {
              const response = await fetch(${toJson(RoutesHelper.getRouteUrlPath(route))}, {
                method: 'POST',
                body: formData
              });

              if (response.ok) {
                console.log('File from clipboard uploaded');
                document.location.reload()
              } else {
                console.error('Error uploading clipboard file');
              }
            } catch (error) {
              console.error('Error:', error);
            }
          }
        }
      }
    });            
        """)

        FORM(route) { route ->
            classes("mb-2 mt-2")
            style("float: right;")

            HIDDEN(route::pageId)

            DIV("input-group") {
                DIV("input-group-prepend") {
                    SUBMIT("btn btn-outline-secondary", i18n.upload)
                }
                INPUT("form-control") {
                    placeholder(i18n.fileUrl)
                    nameValueString(route::fileUrl)
                }
                DIV("custom-file") {
                    INPUT("custom-file-input") {
                        type("file")
                        id("inputGroupFile01")
                        name(route::files.name)
                        multiple(true)
                        onChange("this.form.submit()")
                    }
                    LABEL("custom-file-label") {
                        forAttr("inputGroupFile01")
                        +i18n.chooseFile
                    }
                }
            }
        }

    }
}