CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    manager_id BIGINT,
    default_handler_id BIGINT,
    notification_email VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    department_id BIGINT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    CONSTRAINT fk_user_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
);


ALTER TABLE departments
    ADD CONSTRAINT fk_department_manager
        FOREIGN KEY (manager_id) REFERENCES users(id),
    ADD CONSTRAINT fk_department_default_handler
        FOREIGN KEY (default_handler_id) REFERENCES users(id);


CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100),
    department_id BIGINT NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
);


CREATE TABLE feedbacks (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) UNIQUE NOT NULL,
    received_date TIMESTAMP NOT NULL,
    channel VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    department_id BIGINT NOT NULL,
    doctor_id BIGINT,
    level VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'NEW',
    handler_id BIGINT,
    receiver_id BIGINT NOT NULL,
    process_note TEXT,
    completed_date TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_feedback_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT fk_feedback_handler FOREIGN KEY (handler_id) REFERENCES users(id),
    CONSTRAINT fk_feedback_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
);

CREATE TABLE feedback_images (
    id BIGSERIAL PRIMARY KEY,
    feedback_id BIGINT NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    image_type VARCHAR(20) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_feedback FOREIGN KEY (feedback_id) REFERENCES feedbacks(id),
    CONSTRAINT fk_image_user FOREIGN KEY (uploaded_by) REFERENCES users(id)
);


CREATE TABLE feedback_logs (
    id BIGSERIAL PRIMARY KEY,
    feedback_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    note TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_feedback FOREIGN KEY (feedback_id) REFERENCES feedbacks(id),
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id)
);


