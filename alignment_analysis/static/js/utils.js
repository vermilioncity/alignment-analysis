function getActiveElements(element) {
    return Array.from(document.querySelectorAll(`${element}[class='active']`));
}

function getElementsToActivate(element, selection) {
    if (selection.length > 0) {
        const ids = Array.from(selection).map(e => `${element}[data-name='${e.value}']`).join(', ');
        const nodes = Array.from(document.querySelectorAll(ids));
        return nodes
    }

    return [];

}

function buildUrl(path, params) {

    params = params || {};

    let url = new URL(path, window.location.origin);

    for (const [key, vals] of Object.entries(params)) {
        if (vals.length > 0) {
            url.searchParams.append(key, vals)
        }
    }

    return url
}

export { getElementsToActivate, getActiveElements, buildUrl }