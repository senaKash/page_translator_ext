const MAX_WORDS_PER_REQUEST = 50;

// Функция для рекурсивного обхода DOM и получения текстовых узлов
function getTextNodes(node) {
    let textNodes = [];
    if (node.nodeType === Node.TEXT_NODE && node.nodeValue.trim() !== "") {
        textNodes.push(node);
    } else if (node.nodeType === Node.ELEMENT_NODE) {
        node.childNodes.forEach(child => {
            textNodes = textNodes.concat(getTextNodes(child));
        });
    }
    return textNodes;
}

// Получаем все input и textarea с текстом
function getInputNodes() {
    const inputs = Array.from(document.querySelectorAll("input[type='text'], textarea"));
    return inputs.filter(input => input.value.trim() !== "");
}

// Разбиваем текст на массив порций по MAX_WORDS_PER_REQUEST слов
function chunkText(text, maxWords) {
    const words = text.split(/\s+/);
    const chunks = [];
    for (let i = 0; i < words.length; i += maxWords) {
        chunks.push(words.slice(i, i + maxWords).join(' '));
    }
    return chunks;
}

// Функция перевода одной порции текста через сервер
async function translateChunk(chunk) {
    const response = await fetch("http://localhost:8080/translate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text: chunk })
    });
    const data = await response.json();
    return data.translatedText;
}

// Основная функция перевода всей страницы
async function translatePage() {
    // Перевод текстовых узлов
    const textNodes = getTextNodes(document.body);

    for (const node of textNodes) {
        const originalText = node.nodeValue.trim();
        if (!originalText) continue;

        const chunks = chunkText(originalText, MAX_WORDS_PER_REQUEST);
        let translatedText = "";

        for (const chunk of chunks) {
            try {
                const translatedChunk = await translateChunk(chunk);
                translatedText += translatedChunk + " ";
            } catch (err) {
                console.error("Ошибка при переводе:", err);
                translatedText += chunk + " "; // если не удалось перевести, оставляем оригинал
            }
        }

        node.nodeValue = translatedText.trim();
    }

    // Перевод input и textarea
    const inputNodes = getInputNodes();
    for (const input of inputNodes) {
        const originalText = input.value.trim();
        const chunks = chunkText(originalText, MAX_WORDS_PER_REQUEST);
        let translatedText = "";

        for (const chunk of chunks) {
            try {
                const translatedChunk = await translateChunk(chunk);
                translatedText += translatedChunk + " ";
            } catch (err) {
                console.error("Ошибка при переводе input/textarea:", err);
                translatedText += chunk + " ";
            }
        }

        input.value = translatedText.trim();
    }

    console.log("Перевод страницы завершён!");
}

// Автозапуск перевода сразу при подключении content.js (если нужно)
// translatePage();
