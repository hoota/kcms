package kcms.files

import kcms.common.nullIfBlank
import kcms.ui.KcmsGossRenderer
import kiss.gossr.spring.RoutesHelper

class KcmsFilesListBlock(
    val pageId: Long?,
    val files: List<PageFile>
) : KcmsGossRenderer() {
    fun draw(title: KcmsGossRenderer.() -> Unit) {
        val route = KcmsFilesController.KcmsFilesUploadRoute(pageId = pageId)

        SCRIPT(src = "https://cdn.jsdelivr.net/npm/@fancyapps/ui@5.0/dist/fancybox/fancybox.umd.js")
        LINK(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/@fancyapps/ui@5.0/dist/fancybox/fancybox.css")

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
            if(pageId != null) classes("mt-3")
            style("float: right;")

            HIDDEN(route::pageId)

            DIV("input-group") {
                DIV("input-group-prepend") {
                    SUBMIT("btn btn-outline-secondary", "Upload")
                }
                INPUT("form-control") {
                    placeholder("File URL")
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
                        +"Choose file"
                    }
                }
            }
        }

        title()

        TABLE("table") {
            TBODY {
                files.forEach { f ->
                    TR {
                        TD { +f.id.toString() }
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
                            FORM(KcmsFilesController.KcmsFileRemoveRoute(pageId = f.pageId, fileId = f.id)) { route ->
                                style("float: right;")
                                HIDDEN(route::pageId)
                                HIDDEN(route::fileId)

                                SUBMIT("btn btn-sm btn-danger", "remove") {
                                    onClick("return window.confirm('Are you sure?')")
                                }
                            }
                        }
                    }
                }
            }
        }

        SCRIPT(code = """
            $(document).ready(function() {
                Fancybox.bind("[data-fancybox]", {  });
            });                
        """)

    }
}