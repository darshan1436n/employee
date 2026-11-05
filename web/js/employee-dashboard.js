const h = React.createElement;
const API_URL = 'https://jsonplaceholder.typicode.com/users';

function Navigation({ isForm }) {
  return h('header', { className: 'topbar' },
    h('a', { className: 'brand', href: '/employee-dashboard.html', 'aria-label': 'PeopleDesk home' },
      h('span', { className: 'brand-mark' }, h('i', { className: 'bi bi-person-workspace' })),
      h('span', null, 'people', h('strong', null, 'desk'))
    ),
    h('nav', { className: 'main-nav', 'aria-label': 'Main navigation' },
      h('a', { className: `nav-link${isForm ? '' : ' active'}`, href: '/employee-dashboard.html', 'aria-current': isForm ? undefined : 'page' },
        h('i', { className: 'bi bi-grid-1x2' }), 'Home'
      ),
      h('a', { className: `nav-link${isForm ? ' active' : ''}`, href: '/employee-form.html', 'aria-current': isForm ? 'page' : undefined },
        h('i', { className: 'bi bi-person-plus' }), 'Employee form'
      )
    ),
    h('div', { className: 'profile-chip' },
      h('span', { className: 'profile-avatar' }, 'A'),
      h('span', null, 'Admin')
    )
  );
}

function PageFrame({ isForm, children }) {
  return h(React.Fragment, null,
    h(Navigation, { isForm }),
    h('main', { className: 'page-shell' }, children),
    h('footer', { className: 'site-footer' }, 'PeopleDesk', h('span', null, 'A simple, thoughtful way to manage your team.'))
  );
}

function Dashboard() {
  const [employees, setEmployees] = React.useState([]);
  const [search, setSearch] = React.useState('');
  const [status, setStatus] = React.useState('loading');
  const [error, setError] = React.useState('');

  const loadEmployees = React.useCallback(async (signal) => {
    setStatus('loading');
    setError('');
    try {
      const response = await fetch(API_URL, { signal });
      if (!response.ok) {
        throw new Error(`Employee service returned ${response.status}. Please try again.`);
      }
      const data = await response.json();
      if (!Array.isArray(data)) {
        throw new Error('The employee service returned an unexpected response.');
      }
      setEmployees(data);
      setStatus('ready');
    } catch (requestError) {
      if (requestError.name === 'AbortError') return;
      setError(requestError.message || 'Could not load employees. Check your connection and try again.');
      setStatus('error');
    }
  }, []);

  React.useEffect(() => {
    const controller = new AbortController();
    loadEmployees(controller.signal);
    return () => controller.abort();
  }, [loadEmployees]);

  const filtered = employees.filter((employee) =>
    `${employee.name} ${employee.email}`.toLowerCase().includes(search.trim().toLowerCase())
  );

  return h(PageFrame, { isForm: false },
    h('section', { className: 'welcome-row' },
      h('div', null,
        h('div', { className: 'eyebrow' }, h('span', { className: 'eyebrow-dot' }), 'PEOPLE OVERVIEW'),
        h('h1', null, 'Employee dashboard'),
        h('p', { className: 'page-description' }, 'A clear view of your people, all in one place.')
      ),
      h('a', { className: 'primary-button', href: '/employee-form.html' },
        h('i', { className: 'bi bi-plus-lg' }), ' Add employee'
      )
    ),
    h('section', { className: 'overview-card', 'aria-label': 'Employee overview' },
      h('div', { className: 'overview-icon' }, h('i', { className: 'bi bi-people' })),
      h('div', { className: 'overview-copy' },
        h('span', null, 'TOTAL EMPLOYEES'),
        h('strong', null, status === 'ready' ? employees.length : '—')
      ),
      h('div', { className: 'overview-divider' }),
      h('div', { className: 'overview-note' },
        h('i', { className: 'bi bi-arrow-up-right' }),
        h('span', null, 'Your team, at a glance')
      ),
      h('div', { className: 'overview-decoration', 'aria-hidden': 'true' })
    ),
    h('section', { className: 'directory-section' },
      h('div', { className: 'section-heading' },
        h('div', null,
          h('h2', null, 'Team directory'),
          h('p', null, status === 'ready' ? `Showing ${filtered.length} of ${employees.length} employees` : 'Employee details')
        ),
        h('label', { className: 'search-box' },
          h('i', { className: 'bi bi-search' }),
          h('input', {
            type: 'search',
            placeholder: 'Search name or email',
            value: search,
            onChange: (event) => setSearch(event.target.value),
            'aria-label': 'Search employees by name or email'
          })
        )
      ),
      h('div', { className: 'table-card' },
        h('div', { className: 'employee-table-wrap' },
          h('table', { className: 'employee-table' },
            h('thead', null,
              h('tr', null,
                h('th', { scope: 'col' }, 'EMPLOYEE'),
                h('th', { scope: 'col' }, 'EMAIL ADDRESS'),
                h('th', { scope: 'col', className: 'id-heading' }, 'ID')
              )
            ),
            status === 'loading'
              ? h('tbody', null, h('tr', null, h('td', { colSpan: 3, className: 'table-message' },
                h('span', { className: 'loading-indicator' }), 'Loading your team directory…'
              )))
              : status === 'error'
                ? h('tbody', null, h('tr', null, h('td', { colSpan: 3, className: 'table-message error-message' },
                  h('i', { className: 'bi bi-exclamation-circle' }), ' ', error, ' ',
                  h('button', { className: 'text-button', onClick: () => loadEmployees() }, 'Try again')
                )))
                : h('tbody', null, filtered.length
                  ? filtered.map((employee) => h('tr', { key: employee.id },
                    h('td', null,
                      h('div', { className: 'employee-name' },
                        h('span', { className: 'employee-avatar', 'aria-hidden': 'true' }, employee.name.charAt(0)),
                        h('span', null, employee.name)
                      )
                    ),
                    h('td', null, h('a', { className: 'email-link', href: `mailto:${employee.email}` }, employee.email)),
                    h('td', { className: 'employee-id' }, String(employee.id).padStart(2, '0'))
                  ))
                  : h('tr', null, h('td', { colSpan: 3, className: 'table-message' },
                    status === 'ready' && employees.length ? 'No employees match your search.' : 'No employees to display.'
                  ))
                )
          )
        )
      )
    )
  );
}

function EmployeeForm() {
  const [employee, setEmployee] = React.useState(null);
  const [formKey, setFormKey] = React.useState(0);

  function submitForm(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    setEmployee({
      name: String(formData.get('name')).trim(),
      designation: String(formData.get('designation')).trim(),
      location: String(formData.get('location')).trim(),
      salary: String(formData.get('salary')).trim()
    });
    setFormKey((key) => key + 1);
    event.currentTarget.reset();
  }

  return h(PageFrame, { isForm: true },
    h('section', { className: 'form-page-heading' },
      h('a', { className: 'back-link', href: '/employee-dashboard.html' },
        h('i', { className: 'bi bi-arrow-left' }), ' Back to dashboard'
      ),
      h('div', { className: 'eyebrow' }, h('span', { className: 'eyebrow-dot' }), 'TEAM DIRECTORY'),
      h('h1', null, 'Add an employee'),
      h('p', { className: 'page-description' }, 'Fill in the details below to add someone to your team.')
    ),
    employee && h('div', { className: 'success-banner', role: 'status' },
      h('i', { className: 'bi bi-check-circle-fill' }),
      h('span', null, `${employee.name} has been added successfully.`)
    ),
    h('section', { className: 'form-card' },
      h('div', { className: 'form-card-heading' },
        h('div', { className: 'form-heading-icon' }, h('i', { className: 'bi bi-person-vcard' })),
        h('div', null, h('h2', null, 'Employee details'), h('p', null, 'Fields marked with * are required.'))
      ),
      h('form', { key: formKey, onSubmit: submitForm },
        h('div', { className: 'form-grid' },
          h('div', { className: 'field-group' },
            h('label', { htmlFor: 'employee-name' }, 'Full name ', h('span', null, '*')),
            h('div', { className: 'input-wrap' }, h('i', { className: 'bi bi-person' }),
              h('input', { id: 'employee-name', name: 'name', type: 'text', placeholder: 'e.g. Alex Morgan', autoComplete: 'name', required: true })
            )
          ),
          h('div', { className: 'field-group' },
            h('label', { htmlFor: 'employee-designation' }, 'Designation ', h('span', null, '*')),
            h('div', { className: 'input-wrap' }, h('i', { className: 'bi bi-briefcase' }),
              h('input', { id: 'employee-designation', name: 'designation', type: 'text', placeholder: 'e.g. Product Designer', required: true })
            )
          ),
          h('div', { className: 'field-group' },
            h('label', { htmlFor: 'employee-location' }, 'Location ', h('span', null, '*')),
            h('div', { className: 'input-wrap' }, h('i', { className: 'bi bi-geo-alt' }),
              h('input', { id: 'employee-location', name: 'location', type: 'text', placeholder: 'e.g. New York, NY', autoComplete: 'address-level2', required: true })
            )
          ),
          h('div', { className: 'field-group' },
            h('label', { htmlFor: 'employee-salary' }, 'Annual salary ', h('span', null, '*')),
            h('div', { className: 'input-wrap salary-input' }, h('span', { className: 'currency-symbol' }, '$'),
              h('input', { id: 'employee-salary', name: 'salary', type: 'number', placeholder: 'e.g. 75000', min: '0', step: '0.01', inputMode: 'decimal', required: true })
            ),
            h('small', { className: 'field-hint' }, 'Enter the annual salary in USD.')
          )
        ),
        h('div', { className: 'form-actions' },
          h('a', { className: 'secondary-button', href: '/employee-dashboard.html' }, 'Cancel'),
          h('button', { className: 'primary-button', type: 'submit' }, h('i', { className: 'bi bi-person-plus' }), ' Add employee')
        )
      )
    ),
    employee && h('section', { className: 'added-employee-card', 'aria-live': 'polite' },
      h('div', { className: 'employee-avatar added-avatar' }, employee.name.charAt(0).toUpperCase()),
      h('div', { className: 'added-employee-details' },
        h('strong', null, employee.name),
        h('span', null, employee.designation, ' · ', employee.location)
      ),
      h('strong', { className: 'salary-value' }, `$${Number(employee.salary).toLocaleString('en-US')}`)
    )
  );
}

const isFormRoute = window.location.pathname.endsWith('/employee-form.html');
ReactDOM.createRoot(document.getElementById('root')).render(isFormRoute ? h(EmployeeForm) : h(Dashboard));
