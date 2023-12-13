class MyCustomElement extends HTMLElement {

    constructor() {
        super();
    }

    connectedCallback() {
        const shadow = this.attachShadow({ mode: "open" });
        const e = document.createElement('span');
        e.innerHTML = "I'm in shadow";
        shadow.appendChild(e);
    }

}

customElements.define('my-elem', MyCustomElement);

