package ru.practicum.model;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;

import javax.annotation.processing.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLocation extends EntityPathBase<Location> {

    private static final long serialVersionUID = 583916849L;

    public static final QLocation location = new QLocation("location");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> lat = createNumber("lat", Double.class);

    public final NumberPath<Double> lon = createNumber("lon", Double.class);

    public QLocation(String variable) {
        super(Location.class, forVariable(variable));
    }

    public QLocation(Path<? extends Location> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLocation(PathMetadata metadata) {
        super(Location.class, metadata);
    }

}
