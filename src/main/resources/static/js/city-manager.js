class CityManager {
    constructor() {
        this.baseUrl = '/api/cities';
        this.currentPage = 0;
        this.pageSize = 10;
        this.sortBy = 'id';
        this.sortDir = 'asc';
        this.searchTerm = '';
        this.cityToDelete = null;
        this.connectWebSocket();
        this.init();
    }

    async init() {
        await this.loadCities();
    }

    // Загрузка списка городов
    async loadCities(page = 0) {
        this.showLoading();
        this.currentPage = page;

        const params = new URLSearchParams({
            page: page,
            size: this.pageSize,
            sortBy: this.sortBy,
            sortDir: this.sortDir
        });

        if (this.searchTerm) {
            params.append('search', this.searchTerm);
        }

        try {
            const response = await fetch(`${this.baseUrl}?${params}`);
            if (!response.ok) throw new Error('Network error');

            const data = await response.json();
            this.renderCityList(data);
        } catch (error) {
            this.showError('Error loading cities: ' + error.message);
        }
    }

    // Отображение списка городов
    renderCityList(data) {
        const html = `
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h1><i class="fas fa-city"></i> Cities</h1>
                <button class="btn btn-primary" onclick="cityManager.showCityForm()">
                    <i class="fas fa-plus"></i> Add New City
                </button>
            </div>

            <div class="search-form mb-4">
                <div class="row g-3">
                    <div class="col-md-8">
                        <input type="text" id="searchInput" class="form-control" 
                               value="${this.searchTerm}" placeholder="Search cities by name...">
                    </div>
                    <div class="col-md-4">
                        <button class="btn btn-outline-primary" onclick="cityManager.search()">
                            <i class="fas fa-search"></i> Search
                        </button>
                        <button class="btn btn-outline-secondary" onclick="cityManager.clearSearch()">
                            <i class="fas fa-times"></i> Clear
                        </button>
                    </div>
                </div>
            </div>

            <div class="table-container">
                <div class="table-responsive">
                    <table class="table table-striped table-hover">
                        <thead class="table-dark">
                            <tr>
                                <th><a href="#" onclick="cityManager.sort('id')">ID <i class="fas fa-sort"></i></a></th>
                                <th><a href="#" onclick="cityManager.sort('name')">Name <i class="fas fa-sort"></i></a></th>
                                <th>Coordinates</th>
                                <th>Area</th>
                                <th>Population</th>
                                <th>Climate</th>
                                <th>Capital</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${data.cities && data.cities.length > 0 ?
            data.cities.map(city => this.renderCityRow(city)).join('') :
            '<tr><td colspan="8" class="text-center text-muted">No cities found</td></tr>'
        }
                        </tbody>
                    </table>
                </div>
                ${this.renderPagination(data)}
            </div>

            ${this.renderSpecialOperations()}
        `;

        document.getElementById('app-content').innerHTML = html;
    }

    // Рендер строки города в таблице
    renderCityRow(city) {
        return `
            <tr>
                <td>${city.id}</td>
                <td>${this.escapeHtml(city.name)}</td>
                <td>${city.coordinates ? `X: ${city.coordinates.x}, Y: ${city.coordinates.y}` : 'N/A'}</td>
                <td>${city.area || 'N/A'}</td>
                <td>${city.population ? city.population.toLocaleString() : 'N/A'}</td>
                <td>${city.climate || 'N/A'}</td>
                <td>${city.capital ? '<span class="badge bg-success">Yes</span>' : '<span class="badge bg-secondary">No</span>'}</td>
                <td>
                    <div class="btn-group">
                        <button class="btn btn-sm btn-outline-info" onclick="cityManager.showCityDetail(${city.id})">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-warning" onclick="cityManager.editCity(${city.id})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="cityManager.deleteCity(${city.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    // Пагинация
    renderPagination(data) {
        if (data.totalPages <= 1) return '';

        return `
            <nav aria-label="Page navigation">
                <ul class="pagination justify-content-center">
                    <li class="page-item ${data.currentPage === 0 ? 'disabled' : ''}">
                        <a class="page-link" href="#" onclick="cityManager.loadCities(${data.currentPage - 1})">Previous</a>
                    </li>
                    
                    ${Array.from({length: data.totalPages}, (_, i) => `
                        <li class="page-item ${i === data.currentPage ? 'active' : ''}">
                            <a class="page-link" href="#" onclick="cityManager.loadCities(${i})">${i + 1}</a>
                        </li>
                    `).join('')}
                    
                    <li class="page-item ${data.currentPage === data.totalPages - 1 ? 'disabled' : ''}">
                        <a class="page-link" href="#" onclick="cityManager.loadCities(${data.currentPage + 1})">Next</a>
                    </li>
                </ul>
            </nav>
        `;
    }

    // Специальные операции
    renderSpecialOperations() {
        return `
            <div class="special-operations">
                <h4><i class="fas fa-tools"></i> Special Operations</h4>
                <div class="row">
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">Delete cities by climate:</label>
                            <div class="input-group">
                                <select id="climateSelect" class="form-select">
                                    <option value="">Select climate...</option>
                                    <option value="RAIN_FOREST">Rain Forest</option>
                                    <option value="TROPICAL_SAVANNA">Tropical Savanna</option>
                                    <option value="OCEANIC">Oceanic</option>
                                </select>
                                <button class="btn btn-danger" onclick="cityManager.deleteByClimate()">
                                    <i class="fas fa-trash"></i> Delete
                                </button>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="mb-3">
                            <button class="btn btn-info" onclick="cityManager.getAverageMeters()">
                                <i class="fas fa-chart-line"></i> Average Meters Above Sea Level
                            </button>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="mb-3">
                            <button class="btn btn-warning" onclick="cityManager.getUniqueCarCodes()">
                                <i class="fas fa-list"></i> Unique Car Codes
                            </button>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="mb-3">
                            <button class="btn btn-success" onclick="cityManager.calculateDistances()">
                                <i class="fas fa-route"></i> Calculate Distances
                            </button>
                        </div>
                    </div>
                </div>
                <div id="operationResult" class="operation-result" style="display: none;"></div>
            </div>
        `;
    }

    // Показать детали города
    async showCityDetail(id) {
        this.showLoading();
        try {
            const response = await fetch(`${this.baseUrl}/${id}`);
            if (!response.ok) throw new Error('City not found');

            const city = await response.json();
            this.renderCityDetail(city);
        } catch (error) {
            this.showError('Error loading city: ' + error.message);
        }
    }

    // Рендер деталей города
    renderCityDetail(city) {
        const html = `
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h1><i class="fas fa-city"></i> ${this.escapeHtml(city.name)}</h1>
                <div>
                    <button class="btn btn-warning me-2" onclick="cityManager.editCity(${city.id})">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button class="btn btn-outline-secondary" onclick="cityManager.showCityList()">
                        <i class="fas fa-arrow-left"></i> Back to Cities
                    </button>
                </div>
            </div>

            <div class="detail-container">
                <div class="detail-section">
                    <h5><i class="fas fa-info-circle"></i> Basic Information</h5>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <span class="detail-label">ID:</span>
                                <span class="detail-value">${city.id}</span>
                            </div>
                            <div class="mb-3">
                                <span class="detail-label">Name:</span>
                                <span class="detail-value">${this.escapeHtml(city.name)}</span>
                            </div>
                            <div class="mb-3">
                                <span class="detail-label">Area:</span>
                                <span class="detail-value">${city.area} km²</span>
                            </div>
                            <div class="mb-3">
                                <span class="detail-label">Population:</span>
                                <span class="detail-value">${city.population.toLocaleString()}</span>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <span class="detail-label">Creation Date:</span>
                                <span class="detail-value">${new Date(city.creationDate).toLocaleString()}</span>
                            </div>
                            <div class="mb-3">
                                <span class="detail-label">Capital:</span>
                                <span class="detail-value">
                                    ${city.capital ? '<span class="badge bg-success">Yes</span>' : '<span class="badge bg-secondary">No</span>'}
                                </span>
                            </div>
                            <div class="mb-3">
                                <span class="detail-label">Establishment Date:</span>
                                <span class="detail-value">
                                    ${city.establishmentDate ? new Date(city.establishmentDate).toLocaleString() : 'Not specified'}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                ${city.coordinates ? `
                <div class="detail-section">
                    <h5><i class="fas fa-map-marker-alt"></i> Coordinates</h5>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <span class="detail-label">X Coordinate:</span>
                                <span class="detail-value">${city.coordinates.x}</span>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <span class="detail-label">Y Coordinate:</span>
                                <span class="detail-value">${city.coordinates.y}</span>
                            </div>
                        </div>
                    </div>
                </div>
                ` : ''}

                <div class="detail-section">
                    <h5><i class="fas fa-chart-bar"></i> Additional Information</h5>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <span class="detail-label">Meters Above Sea Level:</span>
                                <span class="detail-value">${city.metersAboveSeaLevel ? city.metersAboveSeaLevel + ' m' : 'Not specified'}</span>
                            </div>
                            <div class="mb-3">
                                <span class="detail-label">Car Code:</span>
                                <span class="detail-value">${city.carCode || 'Not specified'}</span>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <span class="detail-label">Climate:</span>
                                <span class="detail-value">
                                    ${city.climate ? `<span class="badge bg-info">${city.climate}</span>` : 'Not specified'}
                                </span>
                            </div>
                            <div class="mb-3">
                                <span class="detail-label">Standard of Living:</span>
                                <span class="detail-value">
                                    ${city.standardOfLiving ? `<span class="badge bg-warning">${city.standardOfLiving}</span>` : 'Not specified'}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                ${city.governor ? `
                <div class="detail-section">
                    <h5><i class="fas fa-user-tie"></i> Governor Information</h5>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <span class="detail-label">Height:</span>
                                <span class="detail-value">${city.governor.height} m</span>
                            </div>
                        </div>
                    </div>
                </div>
                ` : ''}

                <div class="d-flex justify-content-end">
                    <button class="btn btn-danger" onclick="cityManager.deleteCity(${city.id})">
                        <i class="fas fa-trash"></i> Delete City
                    </button>
                </div>
            </div>
        `;

        document.getElementById('app-content').innerHTML = html;
    }

    // Показать форму создания/редактирования города
    async showCityForm(city = null) {
        const isEdit = city !== null;

        // Загружаем доступные климаты
        let climates = [];
        try {
            const response = await fetch(`${this.baseUrl}/climates`);
            climates = await response.json();
        } catch (error) {
            console.error('Error loading climates:', error);
        }

        const html = `
            <div class="modal-header">
                <h5 class="modal-title">${isEdit ? 'Edit City' : 'Add New City'}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form id="cityForm" onsubmit="event.preventDefault(); cityManager.saveCity()">
                    <input type="hidden" id="cityId" value="${isEdit ? city.id : ''}">
                    
                    <!-- Basic Information -->
                    <div class="form-section mb-4">
                        <h5><i class="fas fa-info-circle"></i> Basic Information</h5>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="name" class="form-label">City Name *</label>
                                    <input type="text" class="form-control" id="name" 
                                           value="${isEdit ? this.escapeHtml(city.name) : ''}" required>
                                    <div class="invalid-feedback" id="nameError"></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="area" class="form-label">Area (km²) *</label>
                                    <input type="number" class="form-control" id="area" 
                                           value="${isEdit ? city.area : ''}" min="1" required>
                                    <div class="invalid-feedback" id="areaError"></div>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="population" class="form-label">Population *</label>
                                    <input type="number" class="form-control" id="population" 
                                           value="${isEdit ? city.population : ''}" min="1" required>
                                    <div class="invalid-feedback" id="populationError"></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="capital" class="form-label">Capital</label>
                                    <select class="form-select" id="capital">
                                        <option value="">Select...</option>
                                        <option value="true" ${isEdit && city.capital ? 'selected' : ''}>Yes</option>
                                        <option value="false" ${isEdit && city.capital === false ? 'selected' : ''}>No</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Coordinates -->
                    <div class="form-section mb-4">
                        <h5><i class="fas fa-map-marker-alt"></i> Coordinates</h5>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="coordinatesX" class="form-label">X Coordinate *</label>
                                    <input type="number" step="0.01" class="form-control" id="coordinatesX" 
                                           value="${isEdit && city.coordinates ? city.coordinates.x : '0'}" required>
                                    <div class="invalid-feedback" id="coordinatesXError"></div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="coordinatesY" class="form-label">Y Coordinate *</label>
                                    <input type="number" class="form-control" id="coordinatesY" 
                                           value="${isEdit && city.coordinates ? city.coordinates.y : '0'}" 
                                           min="-958" required>
                                    <div class="invalid-feedback" id="coordinatesYError"></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Additional Information -->
                    <div class="form-section mb-4">
                        <h5><i class="fas fa-chart-bar"></i> Additional Information</h5>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="metersAboveSeaLevel" class="form-label">Meters Above Sea Level</label>
                                    <input type="number" class="form-control" id="metersAboveSeaLevel" 
                                           value="${isEdit ? city.metersAboveSeaLevel || '' : ''}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="carCode" class="form-label">Car Code</label>
                                    <input type="number" class="form-control" id="carCode" 
                                           value="${isEdit ? city.carCode || '' : ''}" min="1" max="1000">
                                    <div class="invalid-feedback" id="carCodeError"></div>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="climate" class="form-label">Climate</label>
                                    <select class="form-select" id="climate">
                                        <option value="">Select climate...</option>
                                        ${climates.map(climate => `
                                            <option value="${climate}" ${isEdit && city.climate === climate ? 'selected' : ''}>
                                                ${climate}
                                            </option>
                                        `).join('')}
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="standardOfLiving" class="form-label">Standard of Living</label>
                                    <select class="form-select" id="standardOfLiving">
                                        <option value="">Select standard of living...</option>
                                        <option value="HIGH" ${isEdit && city.standardOfLiving === 'HIGH' ? 'selected' : ''}>High</option>
                                        <option value="LOW" ${isEdit && city.standardOfLiving === 'LOW' ? 'selected' : ''}>Low</option>
                                        <option value="VERY_LOW" ${isEdit && city.standardOfLiving === 'VERY_LOW' ? 'selected' : ''}>Very Low</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="establishmentDate" class="form-label">Establishment Date</label>
                                    <input type="datetime-local" class="form-control" id="establishmentDate" 
                                           value="${isEdit && city.establishmentDate ? this.formatDateTimeLocal(city.establishmentDate) : ''}">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="governorHeight" class="form-label">Governor Height (meters)</label>
                                    <input type="number" step="0.01" class="form-control" id="governorHeight" 
                                           value="${isEdit && city.governor ? city.governor.height : ''}" min="0.01">
                                    <div class="invalid-feedback" id="governorHeightError"></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="d-flex justify-content-end gap-2">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save"></i>
                            ${isEdit ? 'Update City' : 'Create City'}
                        </button>
                    </div>
                </form>
            </div>
        `;

        document.getElementById('city-modal-content').innerHTML = html;
        const modal = new bootstrap.Modal(document.getElementById('cityModal'));
        modal.show();
    }

    // Сохранить город
    async saveCity() {
        this.clearErrors();

        const formData = {
            name: document.getElementById('name').value,
            area: parseInt(document.getElementById('area').value),
            population: parseInt(document.getElementById('population').value),
            coordinates: {
                x: parseFloat(document.getElementById('coordinatesX').value),
                y: parseInt(document.getElementById('coordinatesY').value)
            }
        };

        // Опциональные поля
        const capital = document.getElementById('capital').value;
        if (capital !== '') formData.capital = capital === 'true';

        const metersAboveSeaLevel = document.getElementById('metersAboveSeaLevel').value;
        if (metersAboveSeaLevel) formData.metersAboveSeaLevel = parseInt(metersAboveSeaLevel);

        const carCode = document.getElementById('carCode').value;
        if (carCode) formData.carCode = parseInt(carCode);

        const climate = document.getElementById('climate').value;
        if (climate) formData.climate = climate;

        const standardOfLiving = document.getElementById('standardOfLiving').value;
        if (standardOfLiving) formData.standardOfLiving = standardOfLiving;

        const establishmentDate = document.getElementById('establishmentDate').value;
        if (establishmentDate) formData.establishmentDate = establishmentDate + ':00';

        const governorHeight = document.getElementById('governorHeight').value;
        if (governorHeight) {
            formData.governor = { height: parseFloat(governorHeight) };
        }

        const id = document.getElementById('cityId').value;
        const url = id ? `${this.baseUrl}/${id}` : this.baseUrl;
        const method = id ? 'PUT' : 'POST';

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (response.ok) {
                bootstrap.Modal.getInstance(document.getElementById('cityModal')).hide();
                this.showOperationResult('City ' + (id ? 'updated' : 'created') + ' successfully!', true);
                await this.loadCities(this.currentPage);
                this.notifyUpdate();
            } else {
                // Обработка ошибок валидации
                if (result.errors) {
                    this.displayValidationErrors(result.errors);
                } else {
                    this.showOperationResult(result.error || 'Error saving city', false);
                }
            }
        } catch (error) {
            this.showOperationResult('Error saving city: ' + error.message, false);
        }
    }

    // Отображение ошибок валидации
    displayValidationErrors(errors) {
        for (const [field, message] of Object.entries(errors)) {
            const errorElement = document.getElementById(field + 'Error');
            const inputElement = document.getElementById(field);

            if (errorElement && inputElement) {
                errorElement.textContent = message;
                inputElement.classList.add('is-invalid');
            }
        }
    }

    // Очистка ошибок
    clearErrors() {
        document.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });
        document.querySelectorAll('.invalid-feedback').forEach(el => {
            el.textContent = '';
        });
    }

    // Редактировать город
    async editCity(id) {
        try {
            const response = await fetch(`${this.baseUrl}/${id}`);
            if (!response.ok) throw new Error('City not found');

            const cityData = await response.json();  // ← Переименуем в cityData
            this.showCityForm(cityData);  // ← Передаем как простой объект
        } catch (error) {
            this.showOperationResult('Error loading city: ' + error.message, false);
        }
    }

    // Удалить город
    deleteCity(id) {
        this.cityToDelete = id;
        const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
        modal.show();
    }

    // Подтверждение удаления
    async confirmDelete() {
        if (!this.cityToDelete) return;

        try {
            const response = await fetch(`${this.baseUrl}/${this.cityToDelete}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                this.showOperationResult('City deleted successfully!', true);
                await this.loadCities(this.currentPage);
                this.notifyUpdate();
            } else {
                const result = await response.json();
                this.showOperationResult(result.error || 'Error deleting city', false);
            }
        } catch (error) {
            this.showOperationResult('Error deleting city: ' + error.message, false);
        } finally {
            this.cityToDelete = null;
        }
    }

    // Специальные операции
    async deleteByClimate() {
        const climate = document.getElementById('climateSelect').value;
        if (!climate) {
            this.showOperationResult('Please select a climate', false);
            return;
        }

        try {
            const response = await fetch(`${this.baseUrl}/special/delete-by-climate?climate=${climate}`, {
                method: 'POST'
            });
            const result = await response.json();

            this.showOperationResult(result.message, result.success);
            if (result.success) {
                setTimeout(() => this.loadCities(this.currentPage), 2000);
            }
        } catch (error) {
            this.showOperationResult('Error: ' + error.message, false);
        }
    }

    async getAverageMeters() {
        try {
            const response = await fetch(`${this.baseUrl}/special/average-meters`);
            const result = await response.json();
            this.showOperationResult(result.message, result.success);
        } catch (error) {
            this.showOperationResult('Error: ' + error.message, false);
        }
    }

    async getUniqueCarCodes() {
        try {
            const response = await fetch(`${this.baseUrl}/special/unique-car-codes`);
            const result = await response.json();
            const codes = result.carCodes ? result.carCodes.join(', ') : 'None';
            this.showOperationResult(result.message + ': ' + codes, result.success);
        } catch (error) {
            this.showOperationResult('Error: ' + error.message, false);
        }
    }

    async calculateDistances() {
        try {
            const [areaResponse, populationResponse] = await Promise.all([
                fetch(`${this.baseUrl}/special/distance-to-max-area`),
                fetch(`${this.baseUrl}/special/distance-from-origin-to-max-population`)
            ]);

            const areaResult = await areaResponse.json();
            const populationResult = await populationResponse.json();

            let message = '';
            if (areaResult.success) {
                message += `Distance to city with max area: ${areaResult.distance.toFixed(2)}<br>`;
            }
            if (populationResult.success) {
                message += `Distance from origin to city with max population: ${populationResult.distance.toFixed(2)}`;
            }

            this.showOperationResult(message, areaResult.success && populationResult.success);
        } catch (error) {
            this.showOperationResult('Error calculating distances: ' + error.message, false);
        }
    }

    // Поиск и сортировка
    search() {
        this.searchTerm = document.getElementById('searchInput').value;
        this.loadCities(0);
    }

    clearSearch() {
        this.searchTerm = '';
        document.getElementById('searchInput').value = '';
        this.loadCities(0);
    }

    sort(field) {
        this.sortDir = this.sortBy === field && this.sortDir === 'asc' ? 'desc' : 'asc';
        this.sortBy = field;
        this.loadCities(this.currentPage);
    }

    // WebSocket для реальных обновлений
    connectWebSocket() {
        try {
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);

            this.stompClient.connect({}, (frame) => {
                console.log('WebSocket connected: ', frame);

                // Подписываемся на обновления городов
                this.stompClient.subscribe('/topic/city-updates', (message) => {
                    try {
                        const data = JSON.parse(message.body);
                        console.log('WebSocket message received:', data);

                        if (data.type === 'city_updated') {
                            this.showOperationResult('City data updated by another user', true);
                            this.loadCities(this.currentPage);
                        }
                    } catch (e) {
                        console.error('Error processing WebSocket message:', e);
                    }
                });

            }, (error) => {
                console.log('WebSocket connection error: ', error);
                setTimeout(() => this.connectWebSocket(), 5000);
            });

        } catch (error) {
            console.log('WebSocket initialization error: ', error);
            setTimeout(() => this.connectWebSocket(), 5000);
        }
    }

    notifyUpdate() {
        if (this.stompClient && this.stompClient.connected) {
            try {
                this.stompClient.send("/app/city-update", {}, JSON.stringify({
                    type: 'city_updated',
                    timestamp: new Date().toISOString()
                }));
            } catch (error) {
                console.error('Error sending WebSocket message:', error);
            }
        }
    }

    // Вспомогательные методы
    showCityList() {
        this.loadCities();
    }

    showLoading() {
        document.getElementById('app-content').innerHTML = `
            <div class="text-center">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </div>
        `;
    }

    showError(message) {
        document.getElementById('app-content').innerHTML = `
            <div class="alert alert-danger" role="alert">
                <i class="fas fa-exclamation-triangle"></i> ${message}
            </div>
            <button class="btn btn-primary" onclick="cityManager.showCityList()">
                Back to Cities
            </button>
        `;
    }

    showOperationResult(message, isSuccess) {
        const resultDiv = document.getElementById('operationResult');
        if (resultDiv) {
            resultDiv.className = `operation-result ${isSuccess ? 'success' : 'error'}`;
            resultDiv.innerHTML = message;
            resultDiv.style.display = 'block';

            setTimeout(() => {
                resultDiv.style.display = 'none';
            }, 5000);
        }
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    formatDateTimeLocal(dateTimeString) {
        if (!dateTimeString) return '';
        const date = new Date(dateTimeString);
        return date.toISOString().slice(0, 16);
    }
}

// Инициализация при загрузке страницы
let cityManager;

document.addEventListener('DOMContentLoaded', () => {
    cityManager = new CityManager();
});