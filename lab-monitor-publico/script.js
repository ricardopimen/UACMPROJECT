document.addEventListener('DOMContentLoaded', () => {
    const mainPage = document.getElementById('main-page');
    const graphPage = document.getElementById('graph-page');
    const enterButton = document.querySelector('.enter-button[data-target="graph-page"]');
    const backButton = document.getElementById('back-button');

    // Elementos de la gráfica
    let chart;
    let dataPoints = [];
    let time = 0;
    const maxDataPoints = 100;
    let intervalId;
    
    // Elementos de los controles de la gráfica
    const runButton = document.getElementById('run-button');
    const stopButton = document.getElementById('stop-button');
    const applyButton = document.getElementById('apply-button');
    const resetZoomButton = document.getElementById('reset-zoom-button');
    const xAxisMinInput = document.getElementById('x-min');
    const xAxisMaxInput = document.getElementById('x-max');
    const yAxisMinInput = document.getElementById('y-min');
    const yAxisMaxInput = document.getElementById('y-max');
    
    // Elementos de la tabla
    const dataTableBody = document.querySelector('#data-table tbody');

    const canvas = document.getElementById('realTimeChart');
    const canvasContext = canvas.getContext('2d');

    // Lógica del zoom
    let isDrawing = false;
    let isZoomActive = false;
    let startX = 0;
    let startY = 0;

    // Guardar los rangos originales para el reset
    let originalXMin = parseFloat(xAxisMinInput.value);
    let originalXMax = parseFloat(xAxisMaxInput.value);
    let originalYMin = parseFloat(yAxisMinInput.value);
    let originalYMax = parseFloat(yAxisMaxInput.value);

    // Función para mostrar una página y ocultar la otra
    function showPage(pageId) {
        if (pageId === 'main') {
            mainPage.classList.add('active');
            graphPage.classList.remove('active');
            clearInterval(intervalId);
            intervalId = null;
        } else if (pageId === 'graph') {
            mainPage.classList.remove('active');
            graphPage.classList.add('active');
            initChart();
        }
    }

    // Navegación con el botón "Entrar"
    enterButton.addEventListener('click', () => {
        history.pushState({ page: 'graph' }, 'Gráfica', '#graph');
        showPage('graph');
    });

    // Navegación con el botón "Regresar"
    backButton.addEventListener('click', (e) => {
        e.preventDefault();
        history.back();
    });

    // Manejar el evento 'popstate' para el botón de retroceso del navegador
    window.addEventListener('popstate', () => {
        if (location.hash === '#graph') {
            showPage('graph');
        } else {
            showPage('main');
        }
    });

    // Lógica para manejar el estado inicial
    if (location.hash === '#graph') {
        showPage('graph');
    } else {
        showPage('main');
    }

    // Función para iniciar y actualizar la gráfica
    function initChart() {
        const ctx = canvas.getContext('2d');
        
        if (chart) {
            chart.destroy();
        }

        dataPoints = [];
        time = 0;

        const chartConfig = {
            type: 'line',
            data: {
                labels: dataPoints.map((_, i) => i * 1),
                datasets: [{
                    label: 'Velocidad Angular',
                    data: dataPoints,
                    borderColor: 'rgb(255, 77, 77)',
                    backgroundColor: 'rgba(255, 77, 77, 0.2)',
                    borderWidth: 2,
                    pointRadius: 0,
                    fill: false,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 0 },
                scales: {
                    x: {
                        title: { display: true, text: 'Tiempo (centésimas de segundo)', color: '#333' },
                        min: originalXMin,
                        max: originalXMax,
                        ticks: { color: '#333', maxTicksLimit: 10 },
                        grid: { color: 'rgba(0, 0, 0, 0.1)' }
                    },
                    y: {
                        title: { display: true, text: 'Velocidad Angular', color: '#333' },
                        min: originalYMin,
                        max: originalYMax,
                        ticks: { color: '#333' },
                        grid: { color: 'rgba(0, 0, 0, 0.1)' }
                    }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        };

        chart = new Chart(ctx, chartConfig);
        
        if (!intervalId) {
            intervalId = setInterval(() => {
                const newData = Math.random() * 80 + 10;
                dataPoints.push(newData);
                time++;

                if (dataPoints.length > maxDataPoints) {
                    dataPoints.shift();
                }

                chart.data.labels = dataPoints.map((_, i) => (time - dataPoints.length + i) * 1);
                chart.data.datasets[0].data = dataPoints;
                chart.update('none');
            }, 100);
        }
    }

    // --- Lógica del Zoom y Clic en Gráfica ---
    canvas.addEventListener('mousedown', (e) => {
        if (!isZoomActive || !chart) return;
        isDrawing = true;
        const rect = canvas.getBoundingClientRect();
        startX = e.clientX - rect.left;
        startY = e.clientY - rect.top;
    });

    canvas.addEventListener('mousemove', (e) => {
        if (!isDrawing || !chart) return;
        const rect = canvas.getBoundingClientRect();
        const currentX = e.clientX - rect.left;
        const currentY = e.clientY - rect.top;

        chart.update();
        canvasContext.fillStyle = 'rgba(0, 150, 255, 0.3)';
        canvasContext.fillRect(startX, startY, currentX - startX, currentY - startY);
    });

    canvas.addEventListener('mouseup', (e) => {
        if (!isDrawing || !chart) return;
        isDrawing = false;
        chart.update();

        const rect = canvas.getBoundingClientRect();
        const endX = e.clientX - rect.left;
        const endY = e.clientY - rect.top;

        const xMin = Math.min(startX, endX);
        const xMax = Math.max(startX, endX);
        const yMin = Math.min(startY, endY);
        const yMax = Math.max(startY, endY);

        if (xMax - xMin > 0 && yMax - yMin > 0) {
            const newXMin = chart.scales.x.getValueForPixel(xMin);
            const newXMax = chart.scales.x.getValueForPixel(xMax);
            const newYMin = chart.scales.y.getValueForPixel(yMax);
            const newYMax = chart.scales.y.getValueForPixel(yMin);

            chart.options.scales.x.min = newXMin;
            chart.options.scales.x.max = newXMax;
            chart.options.scales.y.min = newYMin;
            chart.options.scales.y.max = newYMax;
            chart.update();

            xAxisMinInput.value = newXMin.toFixed(2);
            xAxisMaxInput.value = newXMax.toFixed(2);
            yAxisMinInput.value = newYMin.toFixed(2);
            yAxisMaxInput.value = newYMax.toFixed(2);
        }
    });
    
    // CORRECCIÓN: Lógica para seleccionar puntos
    canvas.addEventListener('click', (e) => {
        if (!isZoomActive || !chart) return;

        // Se ha cambiado el modo de detección para que funcione mejor en gráficas de línea sin puntos
        const points = chart.getElementsAtEventForMode(e, 'index', { intersect: false }, true);
        
        if (points && points.length > 0) {
            const index = points[0].index;
            const timeValue = chart.data.labels[index];
            const angularVelocity = chart.data.datasets[0].data[index];

            addPointToTable(timeValue.toFixed(2), angularVelocity.toFixed(2));
        }
    });
    
    function addPointToTable(x, y) {
        const row = document.createElement('tr');
        const pointNumber = dataTableBody.children.length + 1;
        row.innerHTML = `
            <td>${pointNumber}</td>
            <td>${x}</td>
            <td>${y}</td>
        `;
        dataTableBody.appendChild(row);
    }

    // --- Funcionalidad de Exportación a CSV (Excel) ---
    function exportToCSV(data) {
        let csvContent = "data:text/csv;charset=utf-8,";
        csvContent += "Tiempo (centésimas de segundo),Velocidad Angular\r\n";
        
        for (let i = 0; i < data.labels.length; i++) {
            const timeValue = data.labels[i];
            const velocityValue = data.datasets[0].data[i];
            csvContent += `${timeValue},${velocityValue}\r\n`;
        }
        
        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", "datos_volante_inercia.csv");
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
    
    // --- Botones de control ---
    runButton.addEventListener('click', () => {
        if (!intervalId) {
            initChart();
            isZoomActive = false;
            canvas.style.cursor = 'default';
        }
    });

    stopButton.addEventListener('click', () => {
        clearInterval(intervalId);
        intervalId = null;
        isZoomActive = true;
        canvas.style.cursor = 'crosshair';
        exportToCSV(chart.data);
    });

    applyButton.addEventListener('click', () => {
        if (chart) {
            chart.options.scales.x.min = parseFloat(xAxisMinInput.value);
            chart.options.scales.x.max = parseFloat(xAxisMaxInput.value);
            chart.options.scales.y.min = parseFloat(yAxisMinInput.value);
            chart.options.scales.y.max = parseFloat(yAxisMaxInput.value);

            chart.update();
        }
    });

    resetZoomButton.addEventListener('click', () => {
        if (chart) {
            chart.options.scales.x.min = originalXMin;
            chart.options.scales.x.max = originalXMax;
            chart.options.scales.y.min = originalYMin;
            chart.options.scales.y.max = originalYMax;
            chart.update();

            xAxisMinInput.value = originalXMin;
            xAxisMaxInput.value = originalXMax;
            yAxisMinInput.value = originalYMin;
            yAxisMaxInput.value = originalYMax;
        }
    });
});