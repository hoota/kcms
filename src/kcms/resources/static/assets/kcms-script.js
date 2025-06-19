function disableEvent(event) {
    event.stopPropagation();
    event.preventDefault();
}

function openCommonModal(event, url) {
    disableEvent(event);
    $(document).trigger('click')

    fetch(url).then(response => {
        if (response.redirected) {
            window.location.href = response.url;
        }
        if(response.ok) {
            response.text().then(html => {
                var placeholder = $("<div/>").append(html)
                $(document.body).append(placeholder);
                placeholder.find(".modal").first().modal('show').on("hidden.bs.modal", function() { placeholder.remove(); });
            })
        } else {
            window.alert("Http Request not OK\nStatus:" + response.status + "\nURL: " + response.url)
        }
    }).catch(function(err) {
        window.alert(err);
    });

    return false
}

function showLoader() {
    if($("#js-gif-loader").length > 0) return
    $(document.body).append($('<div id="js-gif-loader"/>')
        .on('click', disableEvent)
        .on('keydown', disableEvent)
        .on('keypress', disableEvent)
        .on('wheel', disableEvent)
        .on('scroll', disableEvent)
    );
}

function makeLoaderHidden() {
    $("#js-gif-loader").addClass("hide")
}

function removeLoader() {
    $("#js-gif-loader").remove();
}

function ajaxFormSubmit(form, event) {
    try {
        var data = new FormData(form)
        if(event?.submitter?.name) {
            data.append(event?.submitter?.name, event?.submitter?.value)
        }
        form = $(form)
        try { form.closest('.modal').modal('hide'); }catch(e) {}
        $('.modal-backdrop').remove();
        showLoader();
        setTimeout(makeLoaderHidden, 500)
        var actionUrl = form.attr('action');
        var method = form.attr('method') || 'GET';
        var request = method.toLowerCase() === 'get' ? { method: method } : { method: method, body: data }
        fetch(actionUrl, request).then(response => {
            if (response.redirected) {
                window.location.href = response.url;
            }
            const contentType = response.headers.get("content-type")
            console.log("fetch response, OK =", response.ok, ", content-type =", contentType);
            if(response.ok && contentType?.indexOf("text/html") === 0) response.text().then(html => {
                try {
                    var el = $(html);
                    if(el.hasClass('modal')) {
                        var placeholder = $("<div/>").append(el)
                        $(document.body).append(placeholder);
                        placeholder.find(".modal").first().modal('show').on("hidden.bs.modal", function() { placeholder.remove(); });
                    } else {
                        var elId = el.attr('id')
                        $(elId ? ('#' + elId) : form).replaceWith(el)
                    }
                } catch(e) {
                    window.alert("Response is not valid jQuery selector");
                }
            });
            form.find('.js-ajaxFormSubmit-disable').attr('disabled', null).removeClass('js-ajaxFormSubmit-disable');
            removeLoader();
        }).catch(function(err) {
            window.alert(err);
        });

        form.find('button, input[type=submit]:not(:disabled)').attr('disabled', 'disabled').addClass("js-ajaxFormSubmit-disable")
    } catch(e){
        console.error(e)
    }
    return false
}

function moveRowOnTop(btn, submit) {
    const tr = $(btn).closest('tr')[0];
    const firstTr = tr.parentNode.firstChild;
    if (firstTr && firstTr.tagName === 'TR') {
      tr.parentNode.insertBefore(tr, firstTr);
    }
    $('input.order').each(function(index, el) {el.value = index;});

    if(submit) $(btn).closest('form').submit()
}
function moveRowUp(btn, submit) {
    const tr = $(btn).closest('tr')[0];
    const prevTr = tr.previousElementSibling;
    if (prevTr && prevTr.tagName === 'TR') {
      tr.parentNode.insertBefore(tr, prevTr);
    }
    $('input.order').each(function(index, el) {el.value = index;});

    if(submit) $(btn).closest('form').submit()
}
function moveRowDown(btn, submit) {
    const tr = $(btn).closest('tr')[0];
    const nextTr = tr.nextElementSibling;
    if (nextTr && nextTr.tagName === 'TR') {
      tr.parentNode.insertBefore(tr, nextTr.nextElementSibling);
    }
    $('input.order').each(function(index, el) {el.value = index;});

    if(submit) $(btn).closest('form').submit()
}
function moveRowToBottom(btn, submit) {
    const tr = $(btn).closest('tr')[0];
    tr.parentNode.append(tr);
    $('input.order').each(function(index, el) {el.value = index;});

    if(submit) $(btn).closest('form').submit()
}
