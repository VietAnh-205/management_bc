
    create table batch (
        current_quantity integer,
        deleted boolean,
        imported_price integer,
        initial_quantity integer,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        expiry_date timestamp(0),
        id bigint not null,
        import_log_id bigint,
        last_updated timestamp(0),
        last_updater bigint,
        manufacturing_date timestamp(0),
        product_id bigint,
        status varchar(10),
        primary key (id)
    );
    create index idx_batch_product on batch(product_id);
    create index idx_batch_import_log on batch(import_log_id);
    create index idx_batch_product_expiry on batch(product_id, expiry_date);

    create table category (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        description varchar(255),
        name varchar(50) not null,
        primary key (id)
    );
    create unique index uq_category_name on category(name);

    create table customer (
        deleted boolean,
        point integer,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        gender varchar(10),
        name varchar(50),
        phone varchar(12),
        primary key (id)
    );
    create index idx_customer_phone on customer(phone);
    create index idx_customer_name on customer(name);

    create table import_log (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        delivery_date timestamp(0),
        from_stock bigint,
        last_updated timestamp(0),
        last_updater bigint,
        start_date timestamp(0),
        to_store bigint,
        id varchar(255) not null,
        status varchar(10),
        primary key (id)
    );
    create index idx_importlog_to_store on import_log(to_store);
    create index idx_importlog_status_store on import_log(status, to_store);

    create table order_detail (
        deleted boolean,
        quantity integer,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        product_id bigint,
        order_id varchar(255),
        unit_price integer,
        primary key (id)
    );
    create index idx_orderdetail_order on order_detail(order_id);
    create index idx_orderdetail_product on order_detail(product_id);

    create table orders (
        deleted boolean,
        final_price integer,
        created_time timestamp(0),
        creator_id bigint,
        customer_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        last_updated timestamp(0),
        last_updater bigint,
        payment_id bigint,
        store_id bigint not null,
        voucher_id bigint,
        id varchar(255) not null,
        note varchar(50),
        primary key (id)
    );
    create index idx_orders_customer on orders(customer_id);
    create index idx_orders_store on orders(store_id);
    create index idx_orders_payment on orders(payment_id);
    create index idx_orders_store_voucher on orders(store_id, voucher_id);
    create index idx_orders_created_time on orders(created_time desc);

    create table payment_method (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        code varchar(10) not null,
        name varchar(50),
        primary key (id)
    );

    create table permission (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        code varchar(50),
        description varchar(50),
        primary key (id)
    );

    create table product (
        deleted boolean,
        unit_price integer not null,
        category_id bigint,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        supplier_id bigint,
        description varchar(255),
        name varchar(50) not null,
        sku varchar(50) not null unique,
        primary key (id)
    );
    create index idx_product_category on product(category_id);
    create index idx_product_supplier on product(supplier_id);
    create index idx_product_name on product(name);

    create table refresh_token (
        created_time timestamp(0),
        creator_id bigint,
        expired_time timestamp(0) not null,
        device_session varchar(50),
        id varchar(255) not null,
        ip_address varchar(50),
        primary key (id)
    );

    create table role (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        description varchar(255),
        name varchar(50) not null,
        primary key (id)
    );

    create table role_permission (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        permission_id bigint,
        role_id bigint,
        primary key (id)
    );

    create table stock (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        location varchar(50),
        name varchar(50),
        primary key (id)
    );

    create table store (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        address varchar(50),
        name varchar(50) not null,
        phone varchar(12),
        primary key (id)
    );

    create table store_stock (
        deleted boolean,
        quantity integer,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        product_id bigint,
        store_id bigint,
        primary key (id)
    );
    create index idx_storestock_store_product on store_stock(store_id, product_id);
    create index idx_storestock_store on store_stock(store_id);

    create table store_user (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        store_id bigint,
        email varchar(50) unique,
        full_name varchar(50),
        password varchar(50) not null,
        phone varchar(12),
        username varchar(50) not null unique,
        primary key (id)
    );
    create index idx_storeuser_store on store_user(store_id);


    create table supplier (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        address varchar(50),
        contact varchar(50),
        name varchar(50) not null,
        primary key (id)
    );

    create table user_role (
        deleted boolean,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        role_id bigint,
        store_id bigint,
        user_id bigint,
        primary key (id)
    );
    create index idx_userrole_user on user_role(user_id);
    create index idx_userrole_store_role on user_role(store_id, role_id);


    create table voucher (
        deleted boolean,
        discount_percent integer,
        discount_value integer,
        created_time timestamp(0),
        creator_id bigint,
        deleted_time timestamp(0),
        deleter_id bigint,
        expiration_time timestamp(0),
        id bigint not null,
        last_updated timestamp(0),
        last_updater bigint,
        start_time timestamp(0),
        code varchar(50),
        description varchar(255),
        primary key (id)
    );
    create unique index uq_voucher_code on voucher(code);
    create index idx_voucher_time on voucher(start_time, expiration_time);
