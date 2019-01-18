package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.C$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

public final class CollectionTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;

    private static final class Adapter<E> extends TypeAdapter<Collection<E>> {
        private final ObjectConstructor<? extends Collection<E>> constructor;
        private final TypeAdapter<E> elementTypeAdapter;

        public Adapter(Gson context, Type elementType, TypeAdapter<E> elementTypeAdapter, ObjectConstructor<? extends Collection<E>> constructor) {
            this.elementTypeAdapter = new TypeAdapterRuntimeTypeWrapper(context, elementTypeAdapter, elementType);
            this.constructor = constructor;
        }

        public Collection<E> read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            Collection<E> collection = (Collection) this.constructor.construct();
            in.beginArray();
            while (in.hasNext()) {
                collection.add(this.elementTypeAdapter.read(in));
            }
            in.endArray();
            return collection;
        }

        public void write(JsonWriter out, Collection<E> collection) throws IOException {
            if (collection == null) {
                out.nullValue();
                return;
            }
            out.beginArray();
            for (E element : collection) {
                this.elementTypeAdapter.write(out, element);
            }
            out.endArray();
        }
    }

    public CollectionTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
        this.constructorConstructor = constructorConstructor;
    }

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Type type = typeToken.getType();
        Class<? super T> rawType = typeToken.getRawType();
        if (!Collection.class.isAssignableFrom(rawType)) {
            return null;
        }
        Type elementType = C$Gson$Types.getCollectionElementType(type, rawType);
        return new Adapter(gson, elementType, gson.getAdapter(TypeToken.get(elementType)), this.constructorConstructor.get(typeToken));
    }
}
