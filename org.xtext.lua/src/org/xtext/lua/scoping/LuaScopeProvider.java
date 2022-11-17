/*
 * generated by Xtext 2.28.0
 */
package org.xtext.lua.scoping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.ISelectable;
import org.eclipse.xtext.resource.impl.AliasedEObjectDescription;
import org.eclipse.xtext.scoping.impl.MultimapBasedSelectable;
import org.eclipse.xtext.scoping.impl.SimpleLocalScopeProvider;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.xtext.lua.lua.Expression_Function;
import org.xtext.lua.lua.Expression_VariableName;
import org.xtext.lua.lua.Referenceable;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping on how and when
 * to use it.
 */
public class LuaScopeProvider extends SimpleLocalScopeProvider {
    private static final Logger LOGGER = Logger.getLogger(LuaScopeProvider.class.getPackageName());

    @Inject
    private IQualifiedNameConverter nameConverter;


    @Override
    protected ISelectable getAllDescriptions(Resource resource) {
//		System.out.println("CALL: LuaScopeProvider.getAllDescriptions(...)");
        Iterable<EObject> allContents = new Iterable<EObject>() {
            @Override
            public Iterator<EObject> iterator() {
                return EcoreUtil.getAllContents(resource, false);
            }
        };
        // aliased object are tracked here during a first pass and expanded in a second
        var aliases = new HashMap<String, IEObjectDescription>();

        /*
         * First Pass: Add aliases for member syntactic sugar (Foo.bar -> Foo:bar) Track aliasing
         * assignments in `aliases`
         */
        var identifyAliases = new Function<EObject, List<IEObjectDescription>>() {
            @Override
            public List<IEObjectDescription> apply(EObject eObject) {
                var descriptions = new ArrayList<IEObjectDescription>();
                if (eObject instanceof Referenceable) {
                    var refble = (Referenceable) eObject;
                    var fqn = getNameProvider().apply(refble);
                    if (fqn != null) {

                        // create description
                        var description = EObjectDescription.create(fqn, refble);
                        descriptions.add(description);

                        // Add alias for functions in tables because of the member syntactic sugar
                        // E.g. Foo:bar(...)
                        if (refble.getEntryValue() instanceof Expression_Function) {
                            var aliasString = fqn.skipLast(1)
                                .toString() + ":"
                                    + fqn.skipFirst(fqn.getSegmentCount() - 1)
                                        .toString();
                            var aliasQn = nameConverter.toQualifiedName(aliasString);
                            descriptions.add(new AliasedEObjectDescription(aliasQn, description));
                        }

                        // Check if this is an aliasing assignment
                        var value = LuaUtil.resolveRefToValue(refble);
                        if (value instanceof Expression_VariableName) {
                            // extract the reference name from the node model
                            var node = NodeModelUtils.getNode(value);
                            var aliasTarget = NodeModelUtils.getTokenText(node);
                            LOGGER
                                .debug(String.format("Aliasing assignment: %s -> %s", refble.getName(), aliasTarget));
                            aliases.put(aliasTarget, description);
                        }
                    }
                }
                return descriptions;
            }
        };
        var handleAliases = new Function<IEObjectDescription, List<IEObjectDescription>>() {
            @Override
            public List<IEObjectDescription> apply(IEObjectDescription description) {
                var descriptions = new ArrayList<IEObjectDescription>();
                descriptions.add(description);
                // add an alias of the described object is part of an aliased assignment
                var originalName = description.getQualifiedName();
                var aliasingDescription = aliases.get(originalName.getFirstSegment());
                if (aliasingDescription != null) {

                    if (originalName.getSegmentCount() > 1) {
                        // Design decision: The alias points to the original declaration, not the
                        // aliasing declaration
                        // Given Foo = { bar = 42 }; foo = foo
                        // foo.bar points to the refble assigned to 42
//						var aliasTarget = aliasingDescription;
                        var aliasTarget = description;

                        /*
                         * We only join using a ':' if the name is e.g.: Foo:bar -> foo:bar
                         * 
                         * If there are more segments its like: Foo.bar:baz -> foo.bar:baz
                         */
                        var targetString = aliasTarget.getName()
                            .toString();
                        var isMemberAlias = targetString.contains(":") && !targetString.contains(".");

                        var aliasString = aliasingDescription.getName() + (isMemberAlias ? ":" : ".")
                                + originalName.skipFirst(1)
                                    .toString();

//						System.out.printf("Adding description for alias: %s -> %s\n", aliasString,
//								aliasTarget.getName());
                        var aliasQn = nameConverter.toQualifiedName(aliasString);

                        descriptions.add(new AliasedEObjectDescription(aliasQn, aliasTarget));
                    }
                }
                return descriptions;
            }
        };

        // Converting the iterator to a list to synchronize the two passes. This is
        // probably not
        // the correct way of doing this
        var firstPassResult = Lists
            .newArrayList(IterableExtensions.flatten(Iterables.transform(allContents, identifyAliases)));
        var secondPassResult = IterableExtensions.flatten(Iterables.transform(firstPassResult, handleAliases));
        var selectable = new MultimapBasedSelectable(secondPassResult);
        return selectable;
    }
}
