package ru.practicum.model;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathInits;
import com.querydsl.core.types.dsl.StringPath;

import javax.annotation.processing.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompilation extends EntityPathBase<Compilation> {

    private static final long serialVersionUID = -1779695929L;

    public static final QCompilation compilation = new QCompilation("compilation");

    public final ListPath<Event, QEvent> events = this.<Event, QEvent>createList("events", Event.class, QEvent.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath pinned = createBoolean("pinned");

    public final StringPath title = createString("title");

    public QCompilation(String variable) {
        super(Compilation.class, forVariable(variable));
    }

    public QCompilation(Path<? extends Compilation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCompilation(PathMetadata metadata) {
        super(Compilation.class, metadata);
    }

}
