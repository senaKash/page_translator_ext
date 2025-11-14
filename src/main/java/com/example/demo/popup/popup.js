document.getElementById("translatePageBtn").addEventListener("click", () => {
    const maxWords = parseInt(document.getElementById("maxWords").value) || 100;

    chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
        const tabId = tabs[0].id;

        chrome.scripting.executeScript({
            target: { tabId },
            func: async (limit) => {
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

                function chunkText(text, maxWords) {
                    const words = text.split(/\s+/);
                    const chunks = [];
                    for (let i = 0; i < words.length; i += maxWords) {
                        chunks.push(words.slice(i, i + maxWords).join(' '));
                    }
                    return chunks;
                }

                async function translateChunk(chunk) {
                    const response = await fetch("http://localhost:8080/translate", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ text: chunk })
                    });
                    const data = await response.json();
                    return data.translatedText;
                }

                async function translatePage(maxWords = limit) {
                    const textNodes = getTextNodes(document.body);
                    let totalWords = 0;

                    for (const node of textNodes) {
                        const originalText = node.nodeValue.trim();
                        if (!originalText) continue;

                        const nodeWords = originalText.split(/\s+/);
                        if (totalWords + nodeWords.length > maxWords) break;

                        const chunks = chunkText(originalText, 50); // порции по 50 слов
                        let translatedText = "";

                        for (const chunk of chunks) {
                            try {
                                translatedText += await translateChunk(chunk) + " ";
                            } catch (err) {
                                console.error("Ошибка перевода:", err);
                                translatedText += chunk + " ";
                            }
                        }

                        // Создаем span для подсветки
                        const span = document.createElement("span");
                        span.textContent = translatedText.trim();
                        span.style.backgroundColor = "rgba(180, 150, 255, 0.3)"; // светло-фиолетовый
                        node.parentNode.replaceChild(span, node);

                        totalWords += nodeWords.length;
                    }

                    console.log(`Переведено слов: ${totalWords}`);
                }

                translatePage();
            },
            args: [maxWords]
        });
    });
});
