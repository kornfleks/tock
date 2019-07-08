package fr.vsct.tock.bot.admin.story

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.admin.answer.AnswerConfiguration
import fr.vsct.tock.bot.admin.answer.AnswerConfigurationType
import fr.vsct.tock.bot.definition.Intent
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class StoryDefinitionConfiguration_Deserializer :
        JsonDeserializer<StoryDefinitionConfiguration>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(StoryDefinitionConfiguration::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            StoryDefinitionConfiguration {
        with(p) {
            var _storyId_: String? = null
            var _storyId_set : Boolean = false
            var _botId_: String? = null
            var _botId_set : Boolean = false
            var _intent_: Intent? = null
            var _intent_set : Boolean = false
            var _currentType_: AnswerConfigurationType? = null
            var _currentType_set : Boolean = false
            var _answers_: MutableList<AnswerConfiguration>? = null
            var _answers_set : Boolean = false
            var _version_: Int? = null
            var _version_set : Boolean = false
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _mandatoryEntities_: MutableList<StoryDefinitionConfigurationMandatoryEntity>? =
                    null
            var _mandatoryEntities_set : Boolean = false
            var _steps_: MutableList<StoryDefinitionConfigurationStep>? = null
            var _steps_set : Boolean = false
            var _name_: String? = null
            var _name_set : Boolean = false
            var _category_: String? = null
            var _category_set : Boolean = false
            var _description_: String? = null
            var _description_set : Boolean = false
            var _userSentence_: String? = null
            var _userSentence_set : Boolean = false
            var _configurationName_: String? = null
            var _configurationName_set : Boolean = false
            var __id_: Id<StoryDefinitionConfiguration>? = null
            var __id_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "storyId" -> {
                            _storyId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _storyId_set = true
                            }
                    "botId" -> {
                            _botId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _botId_set = true
                            }
                    "intent" -> {
                            _intent_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Intent::class.java);
                            _intent_set = true
                            }
                    "currentType" -> {
                            _currentType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(AnswerConfigurationType::class.java);
                            _currentType_set = true
                            }
                    "answers" -> {
                            _answers_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_answers__reference);
                            _answers_set = true
                            }
                    "version" -> {
                            _version_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _version_set = true
                            }
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "mandatoryEntities" -> {
                            _mandatoryEntities_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_mandatoryEntities__reference);
                            _mandatoryEntities_set = true
                            }
                    "steps" -> {
                            _steps_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_steps__reference);
                            _steps_set = true
                            }
                    "name" -> {
                            _name_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _name_set = true
                            }
                    "category" -> {
                            _category_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _category_set = true
                            }
                    "description" -> {
                            _description_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _description_set = true
                            }
                    "userSentence" -> {
                            _userSentence_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _userSentence_set = true
                            }
                    "configurationName" -> {
                            _configurationName_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _configurationName_set = true
                            }
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_storyId_set && _botId_set && _intent_set && _currentType_set && _answers_set
                    && _version_set && _namespace_set && _mandatoryEntities_set && _steps_set &&
                    _name_set && _category_set && _description_set && _userSentence_set &&
                    _configurationName_set && __id_set)
                    StoryDefinitionConfiguration(storyId = _storyId_!!, botId = _botId_!!, intent =
                            _intent_!!, currentType = _currentType_!!, answers = _answers_!!,
                            version = _version_!!, namespace = _namespace_!!, mandatoryEntities =
                            _mandatoryEntities_!!, steps = _steps_!!, name = _name_!!, category =
                            _category_!!, description = _description_!!, userSentence =
                            _userSentence_!!, configurationName = _configurationName_, _id =
                            __id_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_storyId_set)
                    map[parameters.getValue("storyId")] = _storyId_
                    if(_botId_set)
                    map[parameters.getValue("botId")] = _botId_
                    if(_intent_set)
                    map[parameters.getValue("intent")] = _intent_
                    if(_currentType_set)
                    map[parameters.getValue("currentType")] = _currentType_
                    if(_answers_set)
                    map[parameters.getValue("answers")] = _answers_
                    if(_version_set)
                    map[parameters.getValue("version")] = _version_
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_mandatoryEntities_set)
                    map[parameters.getValue("mandatoryEntities")] = _mandatoryEntities_
                    if(_steps_set)
                    map[parameters.getValue("steps")] = _steps_
                    if(_name_set)
                    map[parameters.getValue("name")] = _name_
                    if(_category_set)
                    map[parameters.getValue("category")] = _category_
                    if(_description_set)
                    map[parameters.getValue("description")] = _description_
                    if(_userSentence_set)
                    map[parameters.getValue("userSentence")] = _userSentence_
                    if(_configurationName_set)
                    map[parameters.getValue("configurationName")] = _configurationName_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<StoryDefinitionConfiguration> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                StoryDefinitionConfiguration::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("storyId" to
                primaryConstructor.findParameterByName("storyId")!!, "botId" to
                primaryConstructor.findParameterByName("botId")!!, "intent" to
                primaryConstructor.findParameterByName("intent")!!, "currentType" to
                primaryConstructor.findParameterByName("currentType")!!, "answers" to
                primaryConstructor.findParameterByName("answers")!!, "version" to
                primaryConstructor.findParameterByName("version")!!, "namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "mandatoryEntities" to
                primaryConstructor.findParameterByName("mandatoryEntities")!!, "steps" to
                primaryConstructor.findParameterByName("steps")!!, "name" to
                primaryConstructor.findParameterByName("name")!!, "category" to
                primaryConstructor.findParameterByName("category")!!, "description" to
                primaryConstructor.findParameterByName("description")!!, "userSentence" to
                primaryConstructor.findParameterByName("userSentence")!!, "configurationName" to
                primaryConstructor.findParameterByName("configurationName")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!) }

        private val _answers__reference: TypeReference<List<AnswerConfiguration>> = object :
                TypeReference<List<AnswerConfiguration>>() {}

        private val _mandatoryEntities__reference:
                TypeReference<List<StoryDefinitionConfigurationMandatoryEntity>> = object :
                TypeReference<List<StoryDefinitionConfigurationMandatoryEntity>>() {}

        private val _steps__reference: TypeReference<List<StoryDefinitionConfigurationStep>> =
                object : TypeReference<List<StoryDefinitionConfigurationStep>>() {}

        private val __id__reference: TypeReference<Id<StoryDefinitionConfiguration>> = object :
                TypeReference<Id<StoryDefinitionConfiguration>>() {}
    }
}
