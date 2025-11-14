document.getElementById("translatePageBtn").addEventListener("click", () => {
    const maxWords = parseInt(document.getElementById("maxWords").value) || 100;

    chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
        const tabId = tabs[0].id;

        chrome.scripting.executeScript({
            target: { tabId },
            func: (limit) => {
                // Получаем все текстовые узлы
                function getTextNodes(element) {
                    const walker = document.createTreeWalker(
                        element,
                        NodeFilter.SHOW_TEXT,
                        { acceptNode: node => node.nodeValue.trim() ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT },
                        false
                    );
                    const nodes = [];
                    while (walker.nextNode()) nodes.push(walker.currentNode);
                    return nodes;
                }

                async function translatePage(maxWords = limit) {
                    const textNodes = getTextNodes(document.body);
                    const batchSize = 5;
                    let wordCount = 0;

                    for (let i = 0; i < textNodes.length; i += batchSize) {
                        const batch = textNodes.slice(i, i + batchSize);
                        const texts = batch.map(node => node.nodeValue);
                        const batchWords = texts.join(" ").split(/\s+/);

                        if (wordCount + batchWords.length > maxWords) break;

                        try {
                            const response = await fetch("http://localhost:8080/translate", {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ text: texts.join("\n") })
                            });

                            const data = await response.json();
                            const translations = data.translatedText.split("\n");

                            translations.forEach((translated, idx) => {
                                const span = document.createElement("span");
                                span.textContent = translated;
                                span.style.backgroundColor = "rgba(107, 79, 255, 0.2)";
                                batch[idx].parentNode.replaceChild(span, batch[idx]);
                            });

                            wordCount += batchWords.length;

                        } catch (err) {
                            console.error("Ошибка перевода:", err);
                        }
                    }

                    console.log(`Переведено слов: ${wordCount}`);
                }

                translatePage();
            },
            args: [maxWords]
        });
    });
});
